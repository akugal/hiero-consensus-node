// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job.metrics;

import java.time.Duration;

public record JobProcessingMetrics(
        Duration jobDuration,
        Duration fetchSuccessTotalDuration,
        Duration fetchErrorTotalDuration,
        Duration processSuccessTotalDuration,
        int distinctUriCount,
        int duplicateUriCount,
        int unsupportedUriCount,
        int fetchErrorsCount,
        int fetchSuccessCount,
        int processErrorsCount) {

    public int getUriCacheHitCount() {
        return distinctUriCount - fetchErrorsCount - fetchSuccessCount;
    }

    public double concurrencyFactor() {
        if (jobDuration.isPositive()) {
            return fetchSuccessTotalDuration
                    .plus(fetchErrorTotalDuration)
                    .plus(processSuccessTotalDuration)
                    .dividedBy(jobDuration);
        }
        return 0;
    }
}
