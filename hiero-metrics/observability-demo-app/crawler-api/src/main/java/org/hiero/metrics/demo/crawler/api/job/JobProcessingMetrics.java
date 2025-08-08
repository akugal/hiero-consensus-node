// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.time.Duration;

public record JobProcessingMetrics(
        Duration jobDuration,
        Duration fetchSuccessTotalDuration,
        Duration fetchErrorTotalDuration,
        Duration processSuccessTotalDuration,
        int distinctUriCount,
        int unsupportedUriCount,
        int duplicateUriCount,
        int fetchErrorsCount,
        int fetchSuccessCount,
        int processErrorsCount) {

    public int getUriCacheHitCount() {
        return distinctUriCount - fetchErrorsCount - fetchSuccessCount;
    }

    public int concurrencyImprovementRatio() {
        if (jobDuration.isPositive()) {
            return (int) fetchSuccessTotalDuration
                    .plus(fetchErrorTotalDuration)
                    .plus(processSuccessTotalDuration)
                    .dividedBy(jobDuration);
        }
        return 0;
    }
}
