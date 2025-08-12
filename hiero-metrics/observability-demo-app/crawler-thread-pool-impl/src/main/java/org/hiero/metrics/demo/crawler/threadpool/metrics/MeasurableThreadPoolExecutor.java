// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import static org.hiero.metrics.demo.crawler.threadpool.metrics.ExecutorServiceFactory.buildThreadFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.ThreadContext;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricRegistryAware;
import org.hiero.metrics.demo.crawler.api.util.Named;
import org.hiero.metrics.demo.crawler.threadpool.config.ThreadPoolConfig;

public class MeasurableThreadPoolExecutor extends ThreadPoolExecutor implements MetricRegistryAware, Named {

    private final String poolName;
    private final ThreadPoolConfig config;

    private ThreadPoolMetrics metrics;

    public MeasurableThreadPoolExecutor(String poolName, ThreadPoolConfig config, RejectedExecutionHandler handler) {
        super(
                config.coreSize(),
                config.maxSize(),
                config.keepAliveSeconds(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(config.queueSize()),
                buildThreadFactory(config),
                handler);
        this.poolName = poolName;
        this.config = config;
    }

    @Override
    public String getName() {
        return poolName;
    }

    public ThreadPoolConfig getConfig() {
        return config;
    }

    @Override
    public synchronized void registerMetrics(MetricRegistry registry) {
        if (metrics == null) {
            metrics = new ThreadPoolMetrics(this, registry);

            final RejectedExecutionHandler handler = getRejectedExecutionHandler();
            setRejectedExecutionHandler((r, executor) -> {
                metrics.taskRejected();
                handler.rejectedExecution(r, executor);
            });
        }
    }

    @Override
    public void execute(@NonNull Runnable command) {
        super.execute(wrap(command));
    }

    private Runnable wrap(Runnable command) {
        final Map<String, String> parentContext = ThreadContext.getImmutableContext();

        if (metrics == null) {
            return () -> {
                final Map<String, String> originalContext = ThreadContext.getImmutableContext();
                ThreadContext.putAll(parentContext);
                try {
                    command.run();
                } finally {
                    ThreadContext.putAll(originalContext);
                }
            };
        } else {
            long submitTime = metrics.taskSubmitted();
            return () -> {
                final Map<String, String> originalContext = ThreadContext.getImmutableContext();
                ThreadContext.putAll(parentContext);

                long startTime = metrics.taskStarted(submitTime);
                try {
                    command.run();
                } finally {
                    ThreadContext.putAll(originalContext);
                    metrics.taskFinished(startTime);
                }
            };
        }
    }
}
