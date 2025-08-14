// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricsRegistrationProvider;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.FrequencyCumulativeAvg;
import org.hiero.metrics.api.stat.FrequencyMovingAvg;
import org.hiero.metrics.api.stat.MovingAverageStat;
import org.hiero.metrics.api.utils.Unit;

public class JobMetricsRegistration implements MetricsRegistrationProvider {

    public static final String CATEGORY = "job";
    public static final String SCHEME_LABEL = "scheme";

    public static final MetricKey<LongCounter> JOBS_COUNT_TOTAL =
            LongCounter.key("count").withCategory(CATEGORY);
    public static final MetricKey<LongCounter> JOBS_TIMEOUT_TOTAL =
            LongCounter.key("timeout_count").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<DoubleSupplier, MovingAverageStat>> JOB_DURATION_AVG_MOVING =
            MovingAverageStat.key("duration_moving_avg").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<Object, FrequencyCumulativeAvg>> JOBS_FREQUENCY_AVG =
            FrequencyCumulativeAvg.key("frequency_avg").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<DoubleSupplier, FrequencyMovingAvg>> JOBS_FREQUENCY_AVG_MOVING =
            FrequencyMovingAvg.key("frequency_moving_avg").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<IntSupplier, CumulativeAverageIntStat>> JOB_CONCURRENCY_FACTOR_AVG =
            CumulativeAverageIntStat.key("concurrency_factor_avg").withCategory(CATEGORY);
    public static final MetricKey<GaugeAdapter<IntSupplier, CumulativeAverageIntStat>> JOB_URI_CACHE_HIT_AVG =
            CumulativeAverageIntStat.key("uri_cache_hit_avg").withCategory(CATEGORY);

    @NonNull
    @Override
    public Collection<Metric.Builder<?, ?>> getMetricsToRegister() {
        return List.of(
                LongCounter.builder(JOBS_COUNT_TOTAL)
                        .withDescription("Total number of jobs executed")
                        .withDynamicLabelNames(SCHEME_LABEL),
                LongCounter.builder(JOBS_TIMEOUT_TOTAL)
                        .withDescription("Total number of jobs timed out")
                        .withDynamicLabelNames(SCHEME_LABEL),
                MovingAverageStat.metricBuilder(1, JOB_DURATION_AVG_MOVING)
                        .withDescription("Job run time moving average with 1 second half-life")
                        .withUnit(Unit.MILLISECOND_UNIT)
                        .withDynamicLabelNames(SCHEME_LABEL),
                FrequencyCumulativeAvg.metricBuilder(JOBS_FREQUENCY_AVG)
                        .withDescription("Jobs frequency average")
                        .withDynamicLabelNames(SCHEME_LABEL),
                FrequencyMovingAvg.metricBuilder(5, JOBS_FREQUENCY_AVG_MOVING)
                        .withDescription("Job frequency moving average (half-life of 5 seconds)")
                        .withDynamicLabelNames(SCHEME_LABEL),
                CumulativeAverageIntStat.metricBuilder(JOB_CONCURRENCY_FACTOR_AVG)
                        .withDescription(
                                "Job concurrency factor average (total tasks processing time divided by job run time)")
                        .withDynamicLabelNames(SCHEME_LABEL),
                CumulativeAverageIntStat.metricBuilder(JOB_URI_CACHE_HIT_AVG)
                        .withDescription("Job uri cache hit average")
                        .withDynamicLabelNames(SCHEME_LABEL));
    }
}
