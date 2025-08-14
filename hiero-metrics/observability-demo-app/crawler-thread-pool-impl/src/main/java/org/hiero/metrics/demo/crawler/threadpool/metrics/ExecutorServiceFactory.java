// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.hiero.metrics.demo.crawler.threadpool.config.ThreadPoolConfig;

public final class ExecutorServiceFactory {

    private ExecutorServiceFactory() {}

    public static <C extends ThreadPoolConfig> ExecutorService buildThreadPoolExecutor(C config) {
        final ThreadFactory threadFactory = buildThreadFactory(config);

        if (config.useVirtualThreads()) {
            return new MonitoredExecutorService(config.getName(), Executors.newThreadPerTaskExecutor(threadFactory));
        } else {
            return new MonitoredExecutorService(
                    config.getName(),
                    new ThreadPoolExecutor(
                            config.coreSize(),
                            config.maxSize(),
                            config.keepAliveSeconds(),
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(config.queueSize() == 0 ? Integer.MAX_VALUE : config.queueSize()),
                            threadFactory,
                            new ThreadPoolExecutor.DiscardPolicy()));
        }
    }

    public static <C extends ThreadPoolConfig> ThreadFactory buildThreadFactory(C config) {
        Thread.Builder builder;
        String prefix = config.getName() + '-';

        if (config.useVirtualThreads()) {
            builder = Thread.ofVirtual();
            prefix += "v-";
        } else {
            builder = Thread.ofPlatform().daemon().priority(Thread.MIN_PRIORITY);
        }

        return builder.inheritInheritableThreadLocals(true).name(prefix, 1).factory();
    }
}
