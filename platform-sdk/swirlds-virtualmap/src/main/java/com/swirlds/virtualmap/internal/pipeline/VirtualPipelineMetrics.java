// SPDX-License-Identifier: Apache-2.0
package com.swirlds.virtualmap.internal.pipeline;

import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_FAMILY_SIZE_BACKPRESSURE_TIME;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_NODE_CACHE_SIZE;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_PIPELINE_SIZE;

import org.hiero.metrics.LongAccumulatorGauge;
import org.hiero.metrics.LongGauge;
import org.hiero.metrics.core.MetricRegistry;

public class VirtualPipelineMetrics {

    private final LongAccumulatorGauge.Measurement familySizeBackpressureMs;
    private final LongGauge.Measurement pipelineSize;
    private final LongGauge.Measurement nodeCacheSize;

    public VirtualPipelineMetrics(MetricRegistry registry) {
        familySizeBackpressureMs =
                registry.getMetric(METRIC_KEY_FAMILY_SIZE_BACKPRESSURE_TIME).getOrCreateNotLabeled();
        pipelineSize = registry.getMetric(METRIC_KEY_PIPELINE_SIZE).getOrCreateNotLabeled();
        nodeCacheSize = registry.getMetric(METRIC_KEY_NODE_CACHE_SIZE).getOrCreateNotLabeled();
    }

    /**
     * Updates {@link #familySizeBackpressureMs} stat.
     *
     * @param backpressureMs family size backpressure, ms
     */
    public void recordFamilySizeBackpressureMs(final long backpressureMs) {
        familySizeBackpressureMs.accumulate(backpressureMs);
    }

    /**
     * Updates {@link #pipelineSize} stat to the given value.
     *
     * @param value the value to set
     */
    public void setPipelineSize(final int value) {
        pipelineSize.set(value);
    }

    /**
     * Updates {@link #nodeCacheSize} stat to the given value.
     *
     * @param value the value to set
     */
    public void setNodeCacheSize(final long value) {
        nodeCacheSize.set(value);
    }
}
