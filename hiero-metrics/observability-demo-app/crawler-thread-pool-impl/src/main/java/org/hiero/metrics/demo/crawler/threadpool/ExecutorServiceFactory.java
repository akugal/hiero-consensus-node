// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import org.hiero.metrics.demo.crawler.threadpool.config.ThreadPoolConfig;
import org.hiero.metrics.demo.crawler.threadpool.metrics.MeasurableThreadPoolExecutor;

public final class ExecutorServiceFactory {

    private ExecutorServiceFactory() {}

    public static <C extends ThreadPoolConfig> ExecutorService buildExecutorService(C config) {
        if (config.useVirtualThreads()) {
            return Executors.newThreadPerTaskExecutor(buildThreadFactory(config));
        } else {
            return new MeasurableThreadPoolExecutor(config.getName(), config, new ThreadPoolExecutor.DiscardPolicy());
        }
    }

    public static <C extends ThreadPoolConfig> ThreadFactory buildThreadFactory(C config) {
        Thread.Builder builder;
        if (config.useVirtualThreads()) {
            builder = Thread.ofVirtual();
        } else {
            builder = Thread.ofPlatform().daemon().priority(Thread.MIN_PRIORITY);
        }

        return builder.inheritInheritableThreadLocals(true)
                .name(config.getName() + '-', 1)
                .factory();
    }
}
