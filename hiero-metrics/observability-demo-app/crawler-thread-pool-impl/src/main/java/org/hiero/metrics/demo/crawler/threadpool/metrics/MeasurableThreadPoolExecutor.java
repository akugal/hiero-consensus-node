// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import static org.hiero.metrics.demo.crawler.threadpool.ExecutorServiceFactory.buildThreadFactory;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
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

    private final ThreadPoolMetrics metrics;

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
        this.metrics = new ThreadPoolMetrics(this);
    }

    @Override
    public String getName() {
        return poolName;
    }

    public ThreadPoolConfig getConfig() {
        return config;
    }

    @Override
    public void registerMetrics(MetricRegistry registry) {
        metrics.registerMetrics(registry);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        long enqueueTime = metrics.currentTime();
        final Map<String, String> parentContext = ThreadContext.getImmutableContext();

        return super.newTaskFor(
                () -> {
                    final Map<String, String> originalContext = ThreadContext.getImmutableContext();
                    ThreadContext.putAll(parentContext);

                    long startTime = metrics.taskStarted(enqueueTime);
                    try {
                        runnable.run();
                    } finally {
                        ThreadContext.putAll(originalContext);
                        metrics.taskFinished(startTime);
                    }
                },
                value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        long enqueueTime = metrics.currentTime();
        final Map<String, String> parentContext = ThreadContext.getImmutableContext();

        return super.newTaskFor(() -> {
            final Map<String, String> originalContext = ThreadContext.getImmutableContext();
            ThreadContext.putAll(parentContext);

            long startTime = metrics.taskStarted(enqueueTime);
            try {
                return callable.call();
            } finally {
                ThreadContext.putAll(originalContext);
                metrics.taskFinished(startTime);
            }
        });
    }
}
