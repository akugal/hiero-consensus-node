// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job.metrics;

import java.net.URI;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.FrequencyCumulativeAvg;
import org.hiero.metrics.api.stat.FrequencyMovingAvg;
import org.hiero.metrics.api.stat.MovingAverageStat;
import org.hiero.metrics.demo.crawler.api.job.JobResult;

public class JobMetricsReporter {

    private final LongCounter countTotal;
    private final GaugeAdapter<DoubleSupplier, MovingAverageStat> durationAvgMoving;
    private final GaugeAdapter<Object, FrequencyCumulativeAvg> frequencyAvg;
    private final GaugeAdapter<DoubleSupplier, FrequencyMovingAvg> frequencyAvgMoving;
    private final GaugeAdapter<IntSupplier, CumulativeAverageIntStat> concurrencyFactor;
    private final GaugeAdapter<IntSupplier, CumulativeAverageIntStat> cacheHitAvg;

    public JobMetricsReporter(MetricRegistry registry) {
        countTotal = registry.getMetric(JobMetricsRegistration.JOBS_COUNT_TOTAL);
        durationAvgMoving = registry.getMetric(JobMetricsRegistration.JOB_DURATION_AVG_MOVING);
        frequencyAvg = registry.getMetric(JobMetricsRegistration.JOBS_FREQUENCY_AVG);
        frequencyAvgMoving = registry.getMetric(JobMetricsRegistration.JOBS_FREQUENCY_AVG_MOVING);
        concurrencyFactor = registry.getMetric(JobMetricsRegistration.JOB_CONCURRENCY_FACTOR_AVG);
        cacheHitAvg = registry.getMetric(JobMetricsRegistration.JOB_URI_CACHE_HIT_AVG);
    }

    private String getScheme(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null || scheme.isEmpty()) {
            scheme = "unknown";
        } else if (scheme.equals("https")) {
            scheme = "http";
        }
        return scheme;
    }

    public void onJobSubmit(URI uri) {
        Map<String, String> labels = Map.of(JobMetricsRegistration.SCHEME_LABEL, getScheme(uri));

        countTotal.getOrCreateLabeled(labels).increment();
        frequencyAvg.getOrCreateLabeled(labels).count();
        frequencyAvgMoving.getOrCreateLabeled(labels).update();
    }

    public void onJobFinish(JobResult jobResult) {
        JobMetrics jobMetrics = jobResult.jobMetrics();
        Map<String, String> labels = Map.of(JobMetricsRegistration.SCHEME_LABEL, getScheme(jobResult.rootUri()));

        durationAvgMoving
                .getOrCreateLabeled(labels)
                .update(jobMetrics.processingMetrics().jobDuration().toMillis());
        concurrencyFactor.getOrCreateLabeled(labels).update((int)
                jobMetrics.processingMetrics().concurrencyFactor());
        cacheHitAvg
                .getOrCreateLabeled(labels)
                .update(jobMetrics.processingMetrics().getUriCacheHitCount());
    }
}
