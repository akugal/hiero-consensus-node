// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.config;

import com.swirlds.config.api.ConfigData;
import com.swirlds.config.api.ConfigProperty;
import com.swirlds.config.api.validation.annotation.Min;

@ConfigData("job.pool")
public record JobPoolConfig(
        @ConfigProperty(defaultValue = "false") boolean useVirtualThreads,
        @ConfigProperty(defaultValue = "0") @Min(0) int coreSize,
        @ConfigProperty(defaultValue = "16") @Min(1) int maxSize,
        @ConfigProperty(defaultValue = "60") @Min(0) int keepAliveSeconds,
        @ConfigProperty(defaultValue = "1024") @Min(0) int queueSize)
        implements ThreadPoolConfig {

    @Override
    public String getName() {
        return "crawler-job-pool";
    }
}
