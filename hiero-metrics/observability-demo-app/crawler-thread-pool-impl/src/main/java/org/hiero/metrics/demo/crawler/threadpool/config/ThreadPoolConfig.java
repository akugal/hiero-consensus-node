// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.config;

public interface ThreadPoolConfig {

    boolean useVirtualThreads();

    String threadPrefix();

    int coreSize();

    int maxSize();

    int keepAliveSeconds();

    int queueSize();
}
