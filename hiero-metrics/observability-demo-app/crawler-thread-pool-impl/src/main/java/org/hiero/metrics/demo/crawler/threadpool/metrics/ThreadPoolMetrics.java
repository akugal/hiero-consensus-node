// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import static org.hiero.metrics.demo.crawler.threadpool.metrics.ThreadPoolMetricsRegistration.POOL_LABEL;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import org.hiero.metrics.api.core.IdempotentMetricRegistryAware;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.stat.RunningAverageStat;
import org.hiero.metrics.demo.crawler.threadpool.config.ThreadPoolConfig;

class ThreadPoolMetrics extends IdempotentMetricRegistryAware {

    private final MeasurableThreadPoolExecutor executor;

    private LongGaugeDataPoint queueSizeMaxSpike;
    private LongGaugeDataPoint queueSizeMinSpike;
    private LongGaugeDataPoint poolSizeMaxSpike;
    private LongGaugeDataPoint poolSizeMinSpike;
    private LongGaugeDataPoint taskWaitTimeMaxSpike;
    private LongGaugeDataPoint taskWaitTimeMinSpike;
    private LongGaugeDataPoint taskRunTimeMaxSpike;
    private LongGaugeDataPoint taskRunTimeMinSpike;
    private RunningAverageStat taskRunTimeRunningAverage;

    public ThreadPoolMetrics(MeasurableThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public long currentTime() {
        return System.nanoTime();
    }

    public long taskStarted(long enqueueTime) {
        long startTime = currentTime();

        if (isMetricsRegistered()) {
            long waitTimeNanos = startTime - enqueueTime;
            taskWaitTimeMaxSpike.update(waitTimeNanos);
            taskWaitTimeMinSpike.update(waitTimeNanos);

            int queueSize = executor.getQueue().size();
            queueSizeMaxSpike.update(queueSize);
            queueSizeMinSpike.update(queueSize);
        }
        return startTime;
    }

    public void taskFinished(long startTime) {
        if (isMetricsRegistered()) {
            long runTimeDuration = currentTime() - startTime;
            taskRunTimeMaxSpike.update(runTimeDuration);
            taskRunTimeMinSpike.update(runTimeDuration);
            taskRunTimeRunningAverage.update(runTimeDuration);

            int poolSize = executor.getPoolSize();
            poolSizeMaxSpike.update(poolSize);
            poolSizeMinSpike.update(poolSize);
        }
    }

    @Override
    protected void registerMetricsNonIdempotent(@NonNull MetricRegistry registry) {
        final ThreadPoolConfig config = executor.getConfig();
        final Map<String, String> poolNameLabels = Map.of(POOL_LABEL, executor.getName());

        registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE)
                .registerDataPoint(() -> executor.getQueue().size(), poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_CONFIG_CAPACITY)
                .registerDataPoint(config::queueSize, poolNameLabels);
        queueSizeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        queueSizeMinSpike = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_MIN_SPIKE)
                .getOrCreateLabeled(poolNameLabels);

        registry.getMetric(ThreadPoolMetricsRegistration.POOL_CONFIG_CORE_SIZE)
                .registerDataPoint(config::coreSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_CONFIG_MAX_SIZE)
                .registerDataPoint(config::maxSize, poolNameLabels);

        registry.getMetric(ThreadPoolMetricsRegistration.POOL_SIZE)
                .registerDataPoint(executor::getPoolSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_MAX_SIZE)
                .registerDataPoint(executor::getLargestPoolSize, poolNameLabels);
        poolSizeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.POOL_SIZE_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        poolSizeMinSpike = registry.getMetric(ThreadPoolMetricsRegistration.POOL_SIZE_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);

        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COUNT_TOTAL)
                .registerDataPoint(executor::getTaskCount, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COMPLETED_COUNT_TOTAL)
                .registerDataPoint(executor::getCompletedTaskCount, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_ACTIVE_COUNT)
                .registerDataPoint(executor::getActiveCount, poolNameLabels);

        taskWaitTimeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_WAIT_TIME_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskWaitTimeMinSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_WAIT_TIME_MIN_SPIKE)
                .getOrCreateLabeled(poolNameLabels);

        taskRunTimeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_RUN_TIME_MAX_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskRunTimeMinSpike = registry.getMetric(ThreadPoolMetricsRegistration.TASK_RUN_TIME_MIN_SPIKE)
                .getOrCreateLabeled(poolNameLabels);
        taskRunTimeRunningAverage = registry.getMetric(ThreadPoolMetricsRegistration.TASK_RUN_TIME_MOVING_AVG)
                .getOrCreateLabeled(poolNameLabels);
    }
}
