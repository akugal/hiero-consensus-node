// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.LongGauge;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricsRegistrationProvider;
import org.hiero.metrics.api.stat.RunningAverageStat;
import org.hiero.metrics.api.utils.Unit;

public class ThreadPoolMetricsRegistration implements MetricsRegistrationProvider {

    public static final String CATEGORY = "thread_pool";
    public static final String POOL_LABEL = "pool_name";

    public static final MetricKey<CallbackMetric> QUEUE_SIZE = CallbackMetric.key(CATEGORY, "queue_size");
    public static final MetricKey<CallbackMetric> QUEUE_CONFIG_CAPACITY =
            CallbackMetric.key(CATEGORY, "queue_config_capacity");
    public static final MetricKey<LongGauge> QUEUE_SIZE_MAX_SPIKE = LongGauge.key(CATEGORY, "queue_size_max_spike");
    public static final MetricKey<LongGauge> QUEUE_SIZE_MIN_SPIKE = LongGauge.key(CATEGORY, "queue_size_min_spike");

    public static final MetricKey<CallbackMetric> POOL_CONFIG_CORE_SIZE =
            CallbackMetric.key(CATEGORY, "pool_config_core_size");
    public static final MetricKey<CallbackMetric> POOL_CONFIG_MAX_SIZE =
            CallbackMetric.key(CATEGORY, "pool_config_max_size");

    public static final MetricKey<CallbackMetric> POOL_SIZE = CallbackMetric.key(CATEGORY, "pool_size");
    public static final MetricKey<CallbackMetric> POOL_MAX_SIZE = CallbackMetric.key(CATEGORY, "pool_size_max");
    public static final MetricKey<LongGauge> POOL_SIZE_MAX_SPIKE = LongGauge.key(CATEGORY, "pool_size_max_spike");
    public static final MetricKey<LongGauge> POOL_SIZE_MIN_SPIKE = LongGauge.key(CATEGORY, "pool_size_min_spike");

    public static final MetricKey<CallbackMetric> TASKS_COUNT_TOTAL = CallbackMetric.key(CATEGORY, "tasks_count_total");
    public static final MetricKey<CallbackMetric> TASKS_COMPLETED_COUNT_TOTAL =
            CallbackMetric.key(CATEGORY, "tasks_completed_count_total");
    public static final MetricKey<CallbackMetric> TASKS_ACTIVE_COUNT =
            CallbackMetric.key(CATEGORY, "tasks_active_count");

    public static final MetricKey<LongGauge> TASK_WAIT_TIME_MAX_SPIKE =
            LongGauge.key(CATEGORY, "task_queue_wait_time_max_spike");
    public static final MetricKey<LongGauge> TASK_WAIT_TIME_MIN_SPIKE =
            LongGauge.key(CATEGORY, "task_queue_wait_time_min_spike");
    public static final MetricKey<LongGauge> TASK_RUN_TIME_MAX_SPIKE =
            LongGauge.key(CATEGORY, "task_run_time_max_spike");
    public static final MetricKey<LongGauge> TASK_RUN_TIME_MIN_SPIKE =
            LongGauge.key(CATEGORY, "task_run_time_min_spike");
    public static final MetricKey<GaugeAdapter<RunningAverageStat>> TASK_RUN_TIME_MOVING_AVG =
            GaugeAdapter.key(CATEGORY, "task_run_time_moving_avg");

    @NonNull
    @Override
    public Collection<Metric.Builder<?, ?>> getMetricsToRegister() {
        return List.of(
                CallbackMetric.builder(QUEUE_SIZE)
                        .withDescription("Thread pool queue size")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(QUEUE_CONFIG_CAPACITY)
                        .withDescription("Thread pool queue config capacity")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(QUEUE_SIZE_MAX_SPIKE, true)
                        .withDescription("Thread pool queue max size spike")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(QUEUE_SIZE_MIN_SPIKE, true)
                        .withDescription("Thread pool queue min size spike")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_CONFIG_CORE_SIZE)
                        .withDescription("Thread pool config core size")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_CONFIG_MAX_SIZE)
                        .withDescription("Thread pool config max size")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_SIZE)
                        .withDescription("Thread pool size")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(POOL_MAX_SIZE)
                        .withDescription("Thread pool max size")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(POOL_SIZE_MAX_SPIKE, true)
                        .withDescription("Thread pool max size spike")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(POOL_SIZE_MIN_SPIKE, true)
                        .withDescription("Thread pool min size spike")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(TASKS_COUNT_TOTAL)
                        .withDescription("Thread pool tasks count total")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(TASKS_ACTIVE_COUNT)
                        .withDescription("Thread pool active task count")
                        .withDynamicLabelNames(POOL_LABEL),
                CallbackMetric.builder(TASKS_COMPLETED_COUNT_TOTAL)
                        .withDescription("Thread pool completed task count")
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(TASK_WAIT_TIME_MAX_SPIKE, true)
                        .withDescription("Thread pool queue task wait time max spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(TASK_WAIT_TIME_MIN_SPIKE, true)
                        .withDescription("Thread pool queue task wait time min spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(TASK_RUN_TIME_MAX_SPIKE, true)
                        .withDescription("Thread pool task run time max spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                LongGauge.maxBuilder(TASK_RUN_TIME_MIN_SPIKE, true)
                        .withDescription("Thread pool task run time min spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL),
                RunningAverageStat.metricBuilder(10, TASK_RUN_TIME_MOVING_AVG)
                        .withDescription("Thread pool task run time moving average")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(POOL_LABEL));
    }
}
