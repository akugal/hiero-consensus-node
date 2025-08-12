// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job.metrics;

import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.stat.FrequencyCumulativeAvg;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.RunningAverageStat;
import org.hiero.metrics.demo.crawler.api.job.JobResult;

public class JobMetricsReporter {

    private final LongCounter countTotal;
    private final GaugeAdapter<DoubleSupplier, RunningAverageStat> durationMovingAvg;
    private final GaugeAdapter<Object, FrequencyCumulativeAvg> countPerSec;
    private final GaugeAdapter<IntSupplier, CumulativeAverageIntStat> concurrencyImprovementAvg;
    private final GaugeAdapter<IntSupplier, CumulativeAverageIntStat> cacheHitAvg;

    public JobMetricsReporter(MetricRegistry registry) {
        countTotal = registry.getMetric(JobMetricsRegistration.JOBS_COUNT_TOTAL);
        durationMovingAvg = registry.getMetric(JobMetricsRegistration.JOB_DURATION_MOVING_AVG);
        countPerSec = registry.getMetric(JobMetricsRegistration.JOBS_FREQUENCY_AVG);
        concurrencyImprovementAvg = registry.getMetric(JobMetricsRegistration.JOB_CONCURRENCY_IMPROVEMENT_AVG);
        cacheHitAvg = registry.getMetric(JobMetricsRegistration.JOB_URI_CACHE_HIT_AVG);
    }

    public void report(JobResult jobResult) {
        String scheme = jobResult.rootUri().getScheme();
        if (scheme == null) {
            scheme = "unknown";
        }

        JobMetrics jobMetrics = jobResult.jobMetrics();
        Map<String, String> labels = Map.of(JobMetricsRegistration.SCHEME_LABEL, scheme);

        countTotal.getOrCreateLabeled(labels).increment();
        durationMovingAvg
                .getOrCreateLabeled(labels)
                .update(jobMetrics.processingMetrics().jobDuration().toMillis());
        countPerSec.getOrCreateLabeled(labels).count();
        concurrencyImprovementAvg
                .getOrCreateLabeled(labels)
                .update(jobMetrics.processingMetrics().concurrencyImprovementRatio());
        cacheHitAvg
                .getOrCreateLabeled(labels)
                .update(jobMetrics.processingMetrics().getUriCacheHitCount());
    }
}
