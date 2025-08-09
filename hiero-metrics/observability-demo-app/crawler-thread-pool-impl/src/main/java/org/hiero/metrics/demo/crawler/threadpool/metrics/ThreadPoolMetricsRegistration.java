// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.LongGauge;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricsRegistrationProvider;
import org.hiero.metrics.api.stat.CountPerSecondCumulativeAvg;
import org.hiero.metrics.api.stat.CountPerSecondWeightedAvg;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.RunningAverageStat;
import org.hiero.metrics.api.utils.Unit;

public class ThreadPoolMetricsRegistration implements MetricsRegistrationProvider {

    public static final String CATEGORY = "thread_pool";
    public static final String POOL_LABEL = "pool_name";

    // config metrics
    public static final MetricKey<CallbackMetric> QUEUE_CONFIG_CAPACITY =
            CallbackMetric.key(CATEGORY, "queue_config_capacity");
    public static final MetricKey<CallbackMetric> POOL_CONFIG_CORE_SIZE =
            CallbackMetric.key(CATEGORY, "pool_config_core_size");
    public static final MetricKey<CallbackMetric> POOL_CONFIG_MAX_SIZE =
            CallbackMetric.key(CATEGORY, "pool_config_max_size");
    public static final MetricKey<CallbackMetric> POOL_CONFIG_KEEP_ALIVE =
            CallbackMetric.key(CATEGORY, "pool_config_keep_alive");

    // queue metrics
    public static final MetricKey<CallbackMetric> QUEUE_SIZE = CallbackMetric.key(CATEGORY, "queue_size");

    public static final MetricKey<LongGauge> QUEUE_SIZE_MAX_SPIKE = LongGauge.key(CATEGORY, "queue_size_max_spike");
    public static final MetricKey<LongGauge> QUEUE_SIZE_MIN_SPIKE = LongGauge.key(CATEGORY, "queue_size_min_spike");
    public static final MetricKey<GaugeAdapter<IntSupplier, CumulativeAverageIntStat>> QUEUE_SIZE_AVG =
            CumulativeAverageIntStat.key(CATEGORY, "queue_size_avg");
    public static final MetricKey<GaugeAdapter<DoubleSupplier, RunningAverageStat>> QUEUE_SIZE_AVG_RUNNING =
            RunningAverageStat.key(CATEGORY, "queue_size_avg_running");

    // pool metrics
    public static final MetricKey<CallbackMetric> POOL_SIZE = CallbackMetric.key(CATEGORY, "pool_size");
    public static final MetricKey<CallbackMetric> POOL_MAX_SIZE = CallbackMetric.key(CATEGORY, "pool_size_max");

    // tasks metrics
    public static final MetricKey<LongCounter> TASKS_COUNT_TOTAL = LongCounter.key(CATEGORY, "tasks_count_total");
    public static final MetricKey<CallbackMetric> TASKS_COUNT_TOTAL_CALLBACK =
            CallbackMetric.key(CATEGORY, "tasks_count_total_callback");
    public static final MetricKey<LongCounter> TASKS_COMPLETED_COUNT_TOTAL =
            LongCounter.key(CATEGORY, "tasks_completed_count_total");
    public static final MetricKey<CallbackMetric> TASKS_COMPLETED_COUNT_TOTAL_CALLBACK =
            CallbackMetric.key(CATEGORY, "tasks_completed_count_total_callback");
    public static final MetricKey<LongGauge> TASKS_ACTIVE_COUNT = LongGauge.key(CATEGORY, "tasks_active_count");
    public static final MetricKey<CallbackMetric> TASKS_ACTIVE_COUNT_CALLBACK =
            CallbackMetric.key(CATEGORY, "tasks_active_count_callback");
    public static final MetricKey<GaugeAdapter<Object, CountPerSecondCumulativeAvg>> TASKS_PER_SECOND_AVG =
            CountPerSecondCumulativeAvg.key(CATEGORY, "tasks_per_sec_avg");
    public static final MetricKey<GaugeAdapter<DoubleSupplier, CountPerSecondWeightedAvg>> TASKS_PER_SECOND_MOVING_AVG =
            CountPerSecondWeightedAvg.key(CATEGORY, "tasks_per_sec_moving_avg");

    // Timing metrics
    public static final MetricKey<LongGauge> TASK_WAIT_TIME_MAX_SPIKE =
            LongGauge.key(CATEGORY, "task_queue_wait_time_max_spike");
    public static final MetricKey<LongGauge> TASK_WAIT_TIME_MIN_SPIKE =
            LongGauge.key(CATEGORY, "task_queue_wait_time_min_spike");
    public static final MetricKey<LongGauge> TASK_DURATION_MAX_SPIKE =
            LongGauge.key(CATEGORY, "task_duration_max_spike");
    public static final MetricKey<LongGauge> TASK_DURATION_MIN_SPIKE =
            LongGauge.key(CATEGORY, "task_duration__min_spike");
    public static final MetricKey<GaugeAdapter<DoubleSupplier, RunningAverageStat>> TASK_DURATION_MOVING_AVG =
            GaugeAdapter.key(CATEGORY, "task_duration_moving_avg");

    @NonNull
    @Override
    public Collection<Metric.Builder<?, ?>> getMetricsToRegister() {
        return List.of(
                //config metrics
                CallbackMetric.builder(QUEUE_CONFIG_CAPACITY)
                        .withDescription("Thread pool config - queue capacity")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_CONFIG_CORE_SIZE)
                        .withDescription("Thread pool config - core size")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_CONFIG_MAX_SIZE)
                        .withDescription("Thread pool config - max size")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_CONFIG_KEEP_ALIVE)
                        .withDescription("Thread pool config - keep alive time in seconds")
                        .withUnit(Unit.SECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                // queue metrics
                CallbackMetric.builder(QUEUE_SIZE)
                        .withDescription("Thread pool queue size")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(QUEUE_SIZE_MAX_SPIKE, true)
                        .withDescription("Thread pool queue max size spike")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.minBuilder(QUEUE_SIZE_MIN_SPIKE, true)
                        .withDescription("Thread pool queue min size spike")
                        .withDynamicLabelNames(POOL_LABEL),
                CumulativeAverageIntStat.metricBuilder(QUEUE_SIZE_AVG)
                        .withDescription("Thread pool queue avg size")
                        .withDynamicLabelNames(POOL_LABEL),
                RunningAverageStat.metricBuilder(5, QUEUE_SIZE_AVG_RUNNING)
                        .withDescription("Thread pool queue running avg size (half-life of 5 seconds)")
                        .withDynamicLabelNames(POOL_LABEL),
                // pool metrics
                CallbackMetric.builder(POOL_SIZE)
                        .withDescription("Thread pool size")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_MAX_SIZE)
                        .withDescription("Thread pool max size")
                        .withDynamicLabelNames(POOL_LABEL),
                // tasks metrics
                LongCounter.builder(TASKS_COUNT_TOTAL)
                        .withDescription("Thread pool tasks count total")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(TASKS_COUNT_TOTAL_CALLBACK)
                        .withDescription("Thread pool tasks count total from callback")
                        .withDynamicLabelNames(POOL_LABEL),
                LongCounter.builder(TASKS_COMPLETED_COUNT_TOTAL)
                        .withDescription("Thread pool completed task count")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(TASKS_COMPLETED_COUNT_TOTAL_CALLBACK)
                        .withDescription("Thread pool completed task count from callback")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.builder(TASKS_ACTIVE_COUNT)
                        .withDescription("Thread pool active task count")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(TASKS_ACTIVE_COUNT_CALLBACK)
                        .withDescription("Thread pool active task count from callback")
                        .withDynamicLabelNames(POOL_LABEL),
                CountPerSecondCumulativeAvg.metricBuilder(TASKS_PER_SECOND_AVG)
                        .withDescription("Thread pool tasks per second cumulative average")
                        .withDynamicLabelNames(POOL_LABEL),
                CountPerSecondWeightedAvg.metricBuilder(5, TASKS_PER_SECOND_MOVING_AVG)
                        .withDescription(
                                "Thread pool tasks per second weighted moving average with half-life of 5 seconds")
                        .withDynamicLabelNames(POOL_LABEL),
                // task timing metrics
                LongGauge.maxBuilder(TASK_WAIT_TIME_MAX_SPIKE, true)
                        .withDescription("Thread pool queue task wait time max spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.minBuilder(TASK_WAIT_TIME_MIN_SPIKE, true)
                        .withDescription("Thread pool queue task wait time min spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(TASK_DURATION_MAX_SPIKE, true)
                        .withDescription("Thread pool task run time max spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.minBuilder(TASK_DURATION_MIN_SPIKE, true)
                        .withDescription("Thread pool task run time min spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                RunningAverageStat.metricBuilder(5, TASK_DURATION_MOVING_AVG)
                        .withDescription("Thread pool task run time moving average with half-life of 5 seconds")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL));
    }
}
