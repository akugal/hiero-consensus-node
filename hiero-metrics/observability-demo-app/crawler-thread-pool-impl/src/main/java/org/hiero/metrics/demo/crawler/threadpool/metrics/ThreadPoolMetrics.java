// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import static org.hiero.metrics.demo.crawler.threadpool.metrics.ThreadPoolMetricsRegistration.NAME_LABEL;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.MovingAverageStat;

class ThreadPoolMetrics extends ExecutorServiceMetrics {

    private final ThreadPoolExecutor threadPoolExecutor;

    // queue metrics
    private final LongGaugeDataPoint queueSizeMaxSpike;
    private final CumulativeAverageIntStat queueSizeAvg;
    private final MovingAverageStat queueSizeAvgMoving;

    public ThreadPoolMetrics(String name, ThreadPoolExecutor threadPoolExecutor, MetricRegistry registry) {
        super(name, registry);
        this.threadPoolExecutor = threadPoolExecutor;

        final Map<String, String> poolNameLabels = Map.of(NAME_LABEL, name);

        // config metrics
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_CONFIG_CORE_SIZE)
                .registerDataPoint(threadPoolExecutor::getCorePoolSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_CONFIG_MAX_SIZE)
                .registerDataPoint(threadPoolExecutor::getMaximumPoolSize, poolNameLabels);

        // queue metrics
        registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE)
                .registerDataPoint(() -> threadPoolExecutor.getQueue().size(), poolNameLabels);
        queueSizeMaxSpike = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_MAX_SPIKE)
                .getOrCreateLabeled(
                        poolNameLabels, () -> threadPoolExecutor.getQueue().size());
        queueSizeAvg = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_AVG)
                .getOrCreateLabeled(
                        poolNameLabels, () -> threadPoolExecutor.getQueue().size());
        queueSizeAvgMoving = registry.getMetric(ThreadPoolMetricsRegistration.QUEUE_SIZE_AVG_MOVING)
                .getOrCreateLabeled(poolNameLabels);

        // pool metrics
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_SIZE)
                .registerDataPoint(threadPoolExecutor::getPoolSize, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.POOL_MAX_SIZE)
                .registerDataPoint(threadPoolExecutor::getLargestPoolSize, poolNameLabels);

        // task callback metrics
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COUNT_TOTAL_CALLBACK)
                .registerDataPoint(threadPoolExecutor::getTaskCount, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_COMPLETED_COUNT_TOTAL_CALLBACK)
                .registerDataPoint(threadPoolExecutor::getCompletedTaskCount, poolNameLabels);
        registry.getMetric(ThreadPoolMetricsRegistration.TASKS_ACTIVE_COUNT_CALLBACK)
                .registerDataPoint(threadPoolExecutor::getActiveCount, poolNameLabels);
    }

    @Override
    public long taskSubmitted() {
        long submitTime = super.taskSubmitted();

        // queue metrics could be also collected with wrapper around queue methods
        int queueSize = threadPoolExecutor.getQueue().size();
        queueSizeMaxSpike.update(queueSize);
        queueSizeAvg.update(queueSize);
        queueSizeAvgMoving.update(queueSize);

        return submitTime;
    }
}
