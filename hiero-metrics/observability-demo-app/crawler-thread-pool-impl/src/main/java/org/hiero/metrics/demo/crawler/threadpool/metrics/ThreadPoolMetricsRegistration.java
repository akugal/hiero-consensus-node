// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.DoubleGauge;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.LongGauge;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricsRegistrationProvider;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.FrequencyCumulativeAvg;
import org.hiero.metrics.api.stat.FrequencyMovingAvg;
import org.hiero.metrics.api.stat.MovingAverageStat;
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.api.utils.Unit;

public class ThreadPoolMetricsRegistration implements MetricsRegistrationProvider {

    public static final String CATEGORY = "thread_pool";
    public static final String NAME_LABEL = "executor_name";

    // config metrics
    public static final MetricKey<CallbackMetric> POOL_CONFIG_CORE_SIZE =
            CallbackMetric.key("pool_config_core_size").withCategory(CATEGORY);
    public static final MetricKey<CallbackMetric> POOL_CONFIG_MAX_SIZE =
            CallbackMetric.key("pool_config_max_size").withCategory(CATEGORY);

    // queue metrics
    public static final MetricKey<CallbackMetric> QUEUE_SIZE =
            CallbackMetric.key("queue_size").withCategory(CATEGORY);

    public static final MetricKey<LongGauge> QUEUE_SIZE_MAX_SPIKE =
            LongGauge.key("queue_size_max_spike").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<IntSupplier, CumulativeAverageIntStat>> QUEUE_SIZE_AVG =
            CumulativeAverageIntStat.key("queue_size_avg").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<DoubleSupplier, MovingAverageStat>> QUEUE_SIZE_AVG_MOVING =
            MovingAverageStat.key("queue_size_moving_avg").withCategory(CATEGORY);

    // pool metrics
    public static final MetricKey<CallbackMetric> POOL_SIZE =
            CallbackMetric.key("pool_size").withCategory(CATEGORY);
    public static final MetricKey<CallbackMetric> POOL_MAX_SIZE =
            CallbackMetric.key("pool_size_max").withCategory(CATEGORY);

    // tasks metrics
    public static final MetricKey<LongCounter> TASKS_COUNT_TOTAL =
            LongCounter.key("tasks_count").withCategory(CATEGORY);
    public static final MetricKey<CallbackMetric> TASKS_COUNT_TOTAL_CALLBACK =
            CallbackMetric.key("tasks_count_callback").withCategory(CATEGORY);
    public static final MetricKey<LongCounter> TASKS_COMPLETED_COUNT_TOTAL =
            LongCounter.key("tasks_completed_count").withCategory(CATEGORY);
    public static final MetricKey<LongCounter> TASKS_REJECTED_COUNT_TOTAL =
            LongCounter.key("tasks_rejected_count").withCategory(CATEGORY);
    public static final MetricKey<CallbackMetric> TASKS_COMPLETED_COUNT_TOTAL_CALLBACK =
            CallbackMetric.key("tasks_completed_count_callback").withCategory(CATEGORY);
    public static final MetricKey<CallbackMetric> TASKS_ACTIVE_COUNT_CALLBACK =
            CallbackMetric.key("tasks_active_count_callback").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<Object, FrequencyCumulativeAvg>> TASKS_FREQUENCY_AVG =
            FrequencyCumulativeAvg.key("tasks_frequency_avg").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<DoubleSupplier, FrequencyMovingAvg>> TASKS_FREQUENCY_MOVING_AVG =
            FrequencyMovingAvg.key("tasks_frequency_moving_avg").withCategory(CATEGORY);

    // Timing metrics
    public static final MetricKey<LongGauge> TASK_WAIT_DURATION_MAX_SPIKE =
            LongGauge.key("task_queue_wait_duration_max_spike").withCategory(CATEGORY);
    public static final MetricKey<LongGauge> TASK_DURATION_MAX_SPIKE =
            LongGauge.key("task_duration_max_spike").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<DoubleSupplier, MovingAverageStat>> TASK_DURATION_MOVING_AVG =
            GaugeAdapter.key("task_duration_moving_avg");
    public static final MetricKey<DoubleGauge> TASK_DURATION_AVG_SMOOTH = DoubleGauge.key("task_duration_avg_smooth");
    public static final MetricKey<DoubleGauge> TASK_DURATION_AVG_VOLATILE =
            DoubleGauge.key("task_duration_avg_volatile");

    @NonNull
    @Override
    public Collection<Metric.Builder<?, ?>> getMetricsToRegister() {
        return List.of(
                // config metrics
                CallbackMetric.builder(POOL_CONFIG_CORE_SIZE)
                        .withDescription("Thread pool config - core size")
                        .withDynamicLabelNames(NAME_LABEL),
                CallbackMetric.builder(POOL_CONFIG_MAX_SIZE)
                        .withDescription("Thread pool config - max size")
                        .withDynamicLabelNames(NAME_LABEL),
                // queue metrics
                CallbackMetric.builder(QUEUE_SIZE)
                        .withDescription("Thread pool queue size")
                        .withDynamicLabelNames(NAME_LABEL),
                LongGauge.maxBuilder(QUEUE_SIZE_MAX_SPIKE, true)
                        .withDescription("Thread pool queue max size spike")
                        .withDynamicLabelNames(NAME_LABEL),
                CumulativeAverageIntStat.metricBuilder(QUEUE_SIZE_AVG)
                        .withDescription("Thread pool queue avg size")
                        .withDynamicLabelNames(NAME_LABEL),
                MovingAverageStat.metricBuilder(1, QUEUE_SIZE_AVG_MOVING)
                        .withDescription("Thread pool queue running avg size (half-life of 1 sec)")
                        .withDynamicLabelNames(NAME_LABEL),
                // pool metrics
                CallbackMetric.builder(POOL_SIZE)
                        .withDescription("Thread pool size")
                        .withDynamicLabelNames(NAME_LABEL),
                CallbackMetric.builder(POOL_MAX_SIZE)
                        .withDescription("Thread pool max size")
                        .withDynamicLabelNames(NAME_LABEL),
                // tasks metrics
                LongCounter.builder(TASKS_COUNT_TOTAL)
                        .withDescription("Thread pool tasks count total")
                        .withDynamicLabelNames(NAME_LABEL),
                CallbackMetric.builder(TASKS_COUNT_TOTAL_CALLBACK)
                        .withDescription("Thread pool tasks count total from callback")
                        .withDynamicLabelNames(NAME_LABEL),
                LongCounter.builder(TASKS_COMPLETED_COUNT_TOTAL)
                        .withDescription("Thread pool completed tasks count")
                        .withDynamicLabelNames(NAME_LABEL),
                CallbackMetric.builder(TASKS_COMPLETED_COUNT_TOTAL_CALLBACK)
                        .withDescription("Thread pool completed tasks count from callback")
                        .withDynamicLabelNames(NAME_LABEL),
                LongCounter.builder(TASKS_REJECTED_COUNT_TOTAL)
                        .withDescription("Thread pool rejected tasks count")
                        .withDynamicLabelNames(NAME_LABEL),
                CallbackMetric.builder(TASKS_ACTIVE_COUNT_CALLBACK)
                        .withDescription("Thread pool active tasks count from callback")
                        .withDynamicLabelNames(NAME_LABEL),
                FrequencyCumulativeAvg.metricBuilder(TASKS_FREQUENCY_AVG)
                        .withDescription("Thread pool tasks frequency cumulative average")
                        .withDynamicLabelNames(NAME_LABEL),
                FrequencyMovingAvg.metricBuilder(1, TASKS_FREQUENCY_MOVING_AVG)
                        .withDescription("Thread pool tasks frequency weighted moving average (half-life of 1 sec)")
                        .withDynamicLabelNames(NAME_LABEL),
                // task timing metrics
                LongGauge.maxBuilder(TASK_WAIT_DURATION_MAX_SPIKE, true)
                        .withInitValue(0L)
                        .withDescription("Thread pool queue task wait duration max spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(NAME_LABEL),
                LongGauge.maxBuilder(TASK_DURATION_MAX_SPIKE, true)
                        .withInitValue(0L)
                        .withDescription("Thread pool task duration time max spike")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(NAME_LABEL),
                MovingAverageStat.metricBuilder(1, TASK_DURATION_MOVING_AVG)
                        .withDescription("Thread pool task run time moving average (half-life of 1 sec)")
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(NAME_LABEL),
                DoubleGauge.builder(TASK_DURATION_AVG_SMOOTH)
                        .withDescription("Thread pool task duration avg smooth")
                        .withOperator(StatUtils.DOUBLE_AVG_SMOOTH)
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(NAME_LABEL),
                DoubleGauge.builder(TASK_DURATION_AVG_VOLATILE)
                        .withDescription("Thread pool task duration avg volatile")
                        .withOperator(StatUtils.DOUBLE_AVG_VOLATILE)
                        .withUnit(Unit.NANOSECOND_UNIT)
                        .withDynamicLabelNames(NAME_LABEL));
    }
}
