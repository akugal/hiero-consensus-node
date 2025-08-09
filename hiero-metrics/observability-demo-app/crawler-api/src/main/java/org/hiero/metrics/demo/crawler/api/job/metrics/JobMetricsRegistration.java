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
import org.hiero.metrics.api.stat.CountPerSecondCumulativeAvg;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.RunningAverageStat;
import org.hiero.metrics.api.utils.Unit;

public class JobMetricsRegistration implements MetricsRegistrationProvider {

    public static final String CATEGORY = "job";
    public static final String SCHEME_LABEL = "scheme";

    public static final MetricKey<LongCounter> JOBS_COUNT_TOTAL = LongCounter.key(CATEGORY, "jobs_count");
    public static final MetricKey<GaugeAdapter<DoubleSupplier, RunningAverageStat>> JOB_DURATION_MOVING_AVG =
            RunningAverageStat.key(CATEGORY, "job_duration_moving_avg");
    public static final MetricKey<GaugeAdapter<Object, CountPerSecondCumulativeAvg>> JOBS_COUNT_PER_SEC_AVG =
            CountPerSecondCumulativeAvg.key(CATEGORY, "job_count_per_second_avg");
    public static final MetricKey<GaugeAdapter<IntSupplier, CumulativeAverageIntStat>> JOB_CONCURRENCY_IMPROVEMENT_AVG =
            CumulativeAverageIntStat.key(CATEGORY, "job_concurrency_improvement_avg");
    public static final MetricKey<GaugeAdapter<IntSupplier, CumulativeAverageIntStat>> JOB_URI_CACHE_HIT_AVG =
            CumulativeAverageIntStat.key(CATEGORY, "job_uri_cache_hit_avg");

    @NonNull
    @Override
    public Collection<Metric.Builder<?, ?>> getMetricsToRegister() {
        return List.of(
                LongCounter.builder(JOBS_COUNT_TOTAL)
                        .withDescription("Total number of jobs executed")
                        .withDynamicLabelNames(SCHEME_LABEL),
                RunningAverageStat.metricBuilder(5, JOB_DURATION_MOVING_AVG)
                        .withDescription("Job run time moving average with 5 seconds half-life")
                        .withUnit(Unit.MILLISECOND_UNIT)
                        .withDynamicLabelNames(SCHEME_LABEL),
                CountPerSecondCumulativeAvg.metricBuilder(JOBS_COUNT_PER_SEC_AVG)
                        .withDescription("Jobs cont per second average")
                        .withDynamicLabelNames(SCHEME_LABEL),
                CumulativeAverageIntStat.metricBuilder(JOB_CONCURRENCY_IMPROVEMENT_AVG)
                        .withDescription(
                                "Job concurrency improvement average (total tasks processing time divided by total job run time)")
                        .withDynamicLabelNames(SCHEME_LABEL),
                CumulativeAverageIntStat.metricBuilder(JOB_URI_CACHE_HIT_AVG)
                        .withDescription("Job uri  cache hit average")
                        .withDynamicLabelNames(SCHEME_LABEL));
    }
}
