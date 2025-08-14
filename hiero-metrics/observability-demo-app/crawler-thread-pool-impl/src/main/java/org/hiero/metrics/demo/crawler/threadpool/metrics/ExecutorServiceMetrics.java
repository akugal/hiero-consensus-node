// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import static org.hiero.metrics.demo.crawler.threadpool.metrics.ThreadPoolMetricsRegistration.NAME_LABEL;

import java.util.Map;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.stat.FrequencyCumulativeAvg;
import org.hiero.metrics.api.stat.FrequencyMovingAvg;
import org.hiero.metrics.api.stat.MovingAverageStat;

class ExecutorServiceMetrics {

    // task metrics
    private final LongCounterDataPoint tasksCount;
    private final LongCounterDataPoint tasksRejectedCount;
    private final LongCounterDataPoint tasksCompletedCount;
    private final FrequencyCumulativeAvg tasksFrequencyAvg;
    private final FrequencyMovingAvg tasksFrequencyMovingAvg;

    // task timing metrics
    private final LongGaugeDataPoint taskWaitTimeMaxSpike;
    private final LongGaugeDataPoint taskDurationMaxSpike;
    private final MovingAverageStat taskDurationRunningAvg;
    private final DoubleGaugeDataPoint taskDurationAvgSmooth;
    private final DoubleGaugeDataPoint taskDurationAvgVolatile;

    public ExecutorServiceMetrics(String name, MetricRegistry registry) {
        final Map<String, String> poolNameLabels = Map.of(NAME_LABEL, name);

        // task metrics
        tasksCount = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        tasksRejectedCount = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_REJECTED_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        tasksCompletedCount = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COMPLETED_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        tasksFrequencyAvg = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_FREQUENCY_AVG)
                .getOrCreateLabeled(poolNameLabels);
        tasksFrequencyMovingAvg = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_FREQUENCY_MOVING_AVG)
                .getOrCreateLabeled(poolNameLabels);

        // task timing metrics
        taskWaitTimeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_WAIT_DURATION_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskDurationMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_DURATION_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskDurationRunningAvg = registry.getMetric(ThreadPoolMetricsRegistration.TASK_DURATION_MOVING_AVG)
                .getOrCreateLabeled(poolNameLabels);
        taskDurationAvgSmooth = registry.getMetric(ThreadPoolMetricsRegistration.TASK_DURATION_AVG_SMOOTH)
                .getOrCreateLabeled(poolNameLabels);
        taskDurationAvgVolatile = registry.getMetric(ThreadPoolMetricsRegistration.TASK_DURATION_AVG_VOLATILE)
                .getOrCreateLabeled(poolNameLabels);
    }

    private long currentTime() {
        return System.nanoTime();
    }

    public void taskRejected() {
        tasksRejectedCount.increment();
    }

    public long taskSubmitted() {
        long submitTime = currentTime();

        tasksCount.increment();
        tasksFrequencyAvg.count();
        tasksFrequencyMovingAvg.update();

        return submitTime;
    }

    public long taskStarted(long submitTime) {
        long startTime = currentTime();
        long waitTimeNanos = startTime - submitTime;

        taskWaitTimeMaxSpike.update(waitTimeNanos);

        return startTime;
    }

    public void taskFinished(long startTime) {
        tasksCompletedCount.increment();

        long runTimeDuration = currentTime() - startTime;
        taskDurationMaxSpike.update(runTimeDuration);
        taskDurationAvgSmooth.update(runTimeDuration);
        taskDurationAvgVolatile.update(runTimeDuration);
        // running average is more expensive - but here for demonstration and testing purposes
        taskDurationRunningAvg.update(runTimeDuration);
    }
}
