// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import static org.hiero.metrics.demo.crawler.threadpool.metrics.ThreadPoolMetricsRegistration.POOL_LABEL;

import java.util.Map;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.FrequencyCumulativeAvg;

class ThreadPoolMetrics {

    private final MeasurableThreadPoolExecutor executor;

    // queue metrics
    private final LongGaugeDataPoint queueSizeMaxSpike;
    private final CumulativeAverageIntStat queueSizeAvg;
    private final DoubleGaugeDataPoint queueSizeAvgMoving;

    // task metrics
    private final LongCounterDataPoint tasksCount;
    private final LongCounterDataPoint tasksCompletedCount;
    private final LongCounterDataPoint tasksRejectedCount;
    private final FrequencyCumulativeAvg tasksFrequencyAvg;
    private final DoubleGaugeDataPoint tasksFrequencyMovingAvg;

    // task timing metrics
    private final LongGaugeDataPoint taskWaitTimeMaxSpike;
    private final LongGaugeDataPoint taskDurationMaxSpike;
    private final DoubleGaugeDataPoint taskDurationRunningAvg;

    public ThreadPoolMetrics(MeasurableThreadPoolExecutor executor, MetricRegistry registry) {
        this.executor = executor;

        final Map<String, String> poolNameLabels = Map.of(POOL_LABEL, executor.getName());

        // config metrics
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_CONFIG_CORE_SIZE)
                .registerDataPoint(executor::getCorePoolSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_CONFIG_MAX_SIZE)
                .registerDataPoint(executor::getMaximumPoolSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_CONFIG_CAPACITY)
                .registerDataPoint(() -> executor.getConfig().queueSize(), poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_CONFIG_KEEP_ALIVE)
                .registerDataPoint(() -> executor.getConfig().keepAliveSeconds(), poolNameLabels);

        // queue metrics
        registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE)
                .registerDataPoint(() -> executor.getQueue().size(), poolNameLabels);
        queueSizeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels, () -> executor.getQueue().size());
        queueSizeAvg = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_AVG)
                .getOrCreateLabeled(poolNameLabels, () -> executor.getQueue().size());
        queueSizeAvgMoving = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_AVG_MOVING)
                .getOrCreateLabeled(poolNameLabels);

        // pool metrics
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_SIZE)
                .registerDataPoint(executor::getPoolSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_MAX_SIZE)
                .registerDataPoint(executor::getLargestPoolSize, poolNameLabels);

        // task metrics
        tasksCount = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COUNT_TOTAL_CALLBACK)
                .registerDataPoint(executor::getTaskCount, poolNameLabels);
        tasksRejectedCount = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_REJECTED_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        tasksCompletedCount = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COMPLETED_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COMPLETED_COUNT_TOTAL_CALLBACK)
                .registerDataPoint(executor::getCompletedTaskCount, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_ACTIVE_COUNT_CALLBACK)
                .registerDataPoint(executor::getActiveCount, poolNameLabels);
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
    }

    private long currentTime() {
        return System.nanoTime();
    }

    public long taskSubmitted() {
        long submitTime = currentTime();

        tasksCount.increment();
        tasksFrequencyAvg.count();
        tasksFrequencyMovingAvg.update();

        int queueSize = executor.getQueue().size();
        queueSizeMaxSpike.update(queueSize);
        queueSizeAvg.update(queueSize);
        queueSizeAvgMoving.update(queueSize);

        return submitTime;
    }

    public void taskRejected() {
        tasksRejectedCount.increment();
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
        taskDurationRunningAvg.update(runTimeDuration);
    }
}
