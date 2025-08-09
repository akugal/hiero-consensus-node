// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job.metrics;

import java.time.Duration;

public record JobTaskMetrics(
        int totalTasksCount,
        int rejectedTasksCount,
        Duration tasksExecutionDelayTotalDuration,
        Duration taskExecutionTotalDuration) {

    public Duration taskExecutionDelayAverageDuration() {
        return tasksExecutionDelayTotalDuration.dividedBy(totalTasksCount);
    }

    public Duration taskExecutionAverageDuration() {
        return taskExecutionTotalDuration.dividedBy(totalTasksCount);
    }
}
