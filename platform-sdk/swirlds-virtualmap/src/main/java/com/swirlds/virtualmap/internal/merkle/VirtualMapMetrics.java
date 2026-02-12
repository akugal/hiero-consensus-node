// SPDX-License-Identifier: Apache-2.0
package com.swirlds.virtualmap.internal.merkle;

import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_ENTITIES_ADDED;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_ENTITIES_COUNT;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_ENTITIES_READ;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_ENTITIES_REMOVED;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_ENTITIES_UPDATED;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_FLUSH_COUNT;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_FLUSH_DURATION_TIME;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_HASH_DURATION_TIME;
import static com.swirlds.virtualmap.VirtualMapMetricsRegistrationProvider.METRIC_KEY_MERGE_DURATION_TIME;

import org.hiero.metrics.LongAccumulatorGauge;
import org.hiero.metrics.LongCounter;
import org.hiero.metrics.LongGauge;
import org.hiero.metrics.core.MetricRegistry;

public class VirtualMapMetrics {

    private final LongGauge.Measurement entitiesSize;

    private final LongCounter.Measurement addedEntities;
    private final LongCounter.Measurement updatedEntities;
    private final LongCounter.Measurement removedEntities;
    private final LongCounter.Measurement readEntities;

    private final LongAccumulatorGauge.Measurement hashDurationMsGauge;
    private final LongAccumulatorGauge.Measurement mergeDurationMsGauge;
    private final LongAccumulatorGauge.Measurement flushDurationMsGauge;
    private final LongCounter.Measurement flushCount;

    public VirtualMapMetrics(MetricRegistry registry) {
        entitiesSize = registry.getMetric(METRIC_KEY_ENTITIES_COUNT).getOrCreateNotLabeled();

        addedEntities = registry.getMetric(METRIC_KEY_ENTITIES_ADDED).getOrCreateNotLabeled();
        updatedEntities = registry.getMetric(METRIC_KEY_ENTITIES_UPDATED).getOrCreateNotLabeled();
        removedEntities = registry.getMetric(METRIC_KEY_ENTITIES_REMOVED).getOrCreateNotLabeled();
        readEntities = registry.getMetric(METRIC_KEY_ENTITIES_READ).getOrCreateNotLabeled();

        hashDurationMsGauge = registry.getMetric(METRIC_KEY_HASH_DURATION_TIME).getOrCreateNotLabeled();
        mergeDurationMsGauge =
                registry.getMetric(METRIC_KEY_MERGE_DURATION_TIME).getOrCreateNotLabeled();
        flushDurationMsGauge =
                registry.getMetric(METRIC_KEY_FLUSH_DURATION_TIME).getOrCreateNotLabeled();
        flushCount = registry.getMetric(METRIC_KEY_FLUSH_COUNT).getOrCreateNotLabeled();
    }

    /**
     * Update the size statistic for the virtual map.
     *
     * @param size the value to set
     */
    public void setSize(final long size) {
        entitiesSize.set(size);
    }

    /**
     * Increments {@link #addedEntities} stat by 1.
     */
    public void countAddedEntities() {
        addedEntities.increment();
    }

    /**
     * Increments {@link #updatedEntities} stat by 1.
     */
    public void countUpdatedEntities() {
        updatedEntities.increment();
    }

    /**
     * Increments {@link #removedEntities} stat by 1.
     */
    public void countRemovedEntities() {
        removedEntities.increment();
    }

    /**
     * Increments {@link #readEntities} stat by 1.
     */
    public void countReadEntities() {
        readEntities.increment();
    }

    /**
     * Record a virtual root copy is merged, and merge duration is as specified.
     *
     * @param mergeDurationMs merge duration, ms
     */
    public void recordMerge(final long mergeDurationMs) {
        mergeDurationMsGauge.accumulate(mergeDurationMs);
    }

    /**
     * Record a virtual root copy is flushed, and flush duration is as specified.
     *
     * @param flushDurationMs flush duration, ms
     */
    public void recordFlush(final long flushDurationMs) {
        flushCount.increment();
        flushDurationMsGauge.accumulate(flushDurationMs);
    }

    /**
     * Record a virtual root copy is hashed, and hash duration is as specified.
     *
     * @param hashDurationMs flush duration, ms
     */
    public void recordHash(final long hashDurationMs) {
        hashDurationMsGauge.accumulate(hashDurationMs);
    }
}
