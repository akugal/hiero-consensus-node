// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.config;

import org.hiero.metrics.demo.crawler.api.util.Named;

public interface ThreadPoolConfig extends Named {

    boolean useVirtualThreads();

    int coreSize();

    int maxSize();

    int keepAliveSeconds();

    int queueSize();
}
