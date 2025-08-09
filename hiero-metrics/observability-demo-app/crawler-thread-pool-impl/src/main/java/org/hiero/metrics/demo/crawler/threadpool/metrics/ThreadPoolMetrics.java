// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import static org.hiero.metrics.demo.crawler.threadpool.metrics.ThreadPoolMetricsRegistration.POOL_LABEL;

import java.util.Map;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.stat.CountPerSecondCumulativeAvg;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;

class ThreadPoolMetrics {

    private final MeasurableThreadPoolExecutor executor;

    private final LongGaugeDataPoint queueSizeMaxSpike;
    private final LongGaugeDataPoint queueSizeMinSpike;
    private final CumulativeAverageIntStat queueSizeAvg;
    private final DoubleGaugeDataPoint queueSizeAvgRunning;

    private final LongCounterDataPoint tasksCountTotal;
    private final LongGaugeDataPoint tasksActiveCount;
    private final LongCounterDataPoint tasksCompletedCountTotal;
    private final LongGaugeDataPoint taskWaitTimeMaxSpike;
    private final LongGaugeDataPoint taskWaitTimeMinSpike;
    private final LongGaugeDataPoint taskDurationMaxSpike;
    private final LongGaugeDataPoint taskDurationMinSpike;
    private final DoubleGaugeDataPoint taskDurationRunningAvg;
    private final DoubleGaugeDataPoint tasksPerSecondMovingAvg;
    private final CountPerSecondCumulativeAvg tasksPerSecondAvg;

    public ThreadPoolMetrics(MeasurableThreadPoolExecutor executor, MetricRegistry registry) {
        this.executor = executor;

        final Map<String, String> poolNameLabels = Map.of(POOL_LABEL, executor.getName());

        //config metrics
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
        queueSizeMinSpike = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_MIN_SPIKE)
                .getOrCreateLabeled(poolNameLabels, () -> executor.getQueue().size());
        queueSizeAvg = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_AVG)
                .getOrCreateLabeled(poolNameLabels, () -> executor.getQueue().size());
        queueSizeAvgRunning = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_AVG_RUNNING)
                .getOrCreateLabeled(poolNameLabels, () -> executor.getQueue().size());

        // pool metrics
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_SIZE)
                .registerDataPoint(executor::getPoolSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_MAX_SIZE)
                .registerDataPoint(executor::getLargestPoolSize, poolNameLabels);

        // task metrics
        tasksCountTotal = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COUNT_TOTAL_CALLBACK)
                .registerDataPoint(executor::getTaskCount, poolNameLabels);
        tasksCompletedCountTotal = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COMPLETED_COUNT_TOTAL)
                .getOrCreateLabeled(poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COMPLETED_COUNT_TOTAL_CALLBACK)
                .registerDataPoint(executor::getCompletedTaskCount, poolNameLabels);
        tasksActiveCount = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_ACTIVE_COUNT)
                .getOrCreateLabeled(poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_ACTIVE_COUNT_CALLBACK)
                .registerDataPoint(executor::getActiveCount, poolNameLabels);

        // task timing metrics
        taskWaitTimeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_WAIT_TIME_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskWaitTimeMinSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_WAIT_TIME_MIN_SPIKE)
                .getOrCreateLabeled(poolNameLabels);

        taskDurationMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_DURATION_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskDurationMinSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_DURATION_MIN_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskDurationRunningAvg = registry.getMetric(ThreadPoolMetricsRegistration.TASK_DURATION_MOVING_AVG)
                .getOrCreateLabeled(poolNameLabels);
        tasksPerSecondMovingAvg = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_PER_SECOND_MOVING_AVG)
                .getOrCreateLabeled(poolNameLabels);
        tasksPerSecondAvg = registry.getMetric(ThreadPoolMetricsRegistration.TASKS_PER_SECOND_AVG)
                .getOrCreateLabeled(poolNameLabels);
    }

    private long currentTime() {
        return System.nanoTime();
    }

    public long taskSubmitted() {
        long submitTime = currentTime();

        tasksCountTotal.increment();
        tasksPerSecondAvg.count();
        tasksPerSecondMovingAvg.update();

        int queueSize = executor.getQueue().size();
        queueSizeMaxSpike.update(queueSize);
        queueSizeMinSpike.update(queueSize);
        queueSizeAvg.update(queueSize);
        queueSizeAvgRunning.update(queueSize);

        return submitTime;
    }

    public long taskStarted(long submitTime) {
        tasksActiveCount.increment();

        long startTime = currentTime();
        long waitTimeNanos = startTime - submitTime;
        taskWaitTimeMaxSpike.update(waitTimeNanos);
        taskWaitTimeMinSpike.update(waitTimeNanos);

        return startTime;
    }

    public void taskFinished(long startTime) {
        tasksCompletedCountTotal.increment();
        tasksActiveCount.decrement();

        long runTimeDuration = currentTime() - startTime;
        taskDurationMaxSpike.update(runTimeDuration);
        taskDurationMinSpike.update(runTimeDuration);
        taskDurationRunningAvg.update(runTimeDuration);
    }
}
