// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.ThreadContext;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricRegistryAware;
import org.hiero.metrics.demo.crawler.api.util.Named;

public class MonitoredExecutorService extends AbstractExecutorService
        implements ExecutorService, MetricRegistryAware, Named {

    private final String executorName;
    private final ExecutorService delegate;

    private ExecutorServiceMetrics metrics;

    public MonitoredExecutorService(String executorName, ExecutorService delegate) {
        this.executorName = executorName;
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return executorName;
    }

    @Override
    public synchronized void registerMetrics(MetricRegistry registry) {
        if (metrics == null) {
            if (delegate instanceof ThreadPoolExecutor threadPoolExecutor) {
                metrics = new ThreadPoolMetrics(getName(), threadPoolExecutor, registry);

                final RejectedExecutionHandler rejectHandler = threadPoolExecutor.getRejectedExecutionHandler();
                threadPoolExecutor.setRejectedExecutionHandler((r, executor) -> {
                    metrics.taskRejected();
                    rejectHandler.rejectedExecution(r, executor);
                });
            } else {
                metrics = new ExecutorServiceMetrics(getName(), registry);
            }
        }
    }

    @Override
    public void execute(@NonNull Runnable command) {
        delegate.execute(wrap(command));
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

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }
}
