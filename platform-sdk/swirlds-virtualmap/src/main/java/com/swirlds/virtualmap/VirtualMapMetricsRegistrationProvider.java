// SPDX-License-Identifier: Apache-2.0
package com.swirlds.virtualmap;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Collection;
import org.hiero.metrics.LongAccumulatorGauge;
import org.hiero.metrics.LongCounter;
import org.hiero.metrics.LongGauge;
import org.hiero.metrics.core.Metric;
import org.hiero.metrics.core.MetricKey;
import org.hiero.metrics.core.MetricsRegistrationProvider;
import org.hiero.metrics.core.Unit;

public class VirtualMapMetricsRegistrationProvider implements MetricsRegistrationProvider {

    private static final String MAIN_CATEGORY = "virtual_map";
    /** Prefix for all metrics related to virtual map queries */
    private static final String QUERIES_CATEGORY = "queries";
    /** Prefix for all lifecycle related metric names */
    private static final String LIFECYCLE_CATEGORY = "lifecycle";

    public static final MetricKey<LongGauge> METRIC_KEY_ENTITIES_COUNT =
            LongGauge.key("entitiesCount").addCategory(MAIN_CATEGORY);

    // Queries metric keys
    public static final MetricKey<LongCounter> METRIC_KEY_ENTITIES_ADDED =
            LongCounter.key("addedEntities").addCategory(QUERIES_CATEGORY).addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongCounter> METRIC_KEY_ENTITIES_UPDATED =
            LongCounter.key("updatedEntities").addCategory(QUERIES_CATEGORY).addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongCounter> METRIC_KEY_ENTITIES_REMOVED =
            LongCounter.key("removedEntities").addCategory(QUERIES_CATEGORY).addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongCounter> METRIC_KEY_ENTITIES_READ =
            LongCounter.key("readEntities").addCategory(QUERIES_CATEGORY).addCategory(MAIN_CATEGORY);

    // Lifecycle metric keys
    public static final MetricKey<LongGauge> METRIC_KEY_NODE_CACHE_SIZE =
            LongGauge.key("nodeCacheSize").addCategory(LIFECYCLE_CATEGORY).addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongGauge> METRIC_KEY_PIPELINE_SIZE =
            LongGauge.key("pipelineSize").addCategory(LIFECYCLE_CATEGORY).addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongAccumulatorGauge> METRIC_KEY_FLUSH_BACKPRESSURE_TIME = LongAccumulatorGauge.key(
                    "flushBackpressureTime")
            .addCategory(LIFECYCLE_CATEGORY)
            .addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongAccumulatorGauge> METRIC_KEY_FAMILY_SIZE_BACKPRESSURE_TIME =
            LongAccumulatorGauge.key("familySizeBackpressureTime")
                    .addCategory(LIFECYCLE_CATEGORY)
                    .addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongAccumulatorGauge> METRIC_KEY_MERGE_DURATION_TIME = LongAccumulatorGauge.key(
                    "mergeDurationTime")
            .addCategory(LIFECYCLE_CATEGORY)
            .addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongAccumulatorGauge> METRIC_KEY_FLUSH_DURATION_TIME = LongAccumulatorGauge.key(
                    "flushDurationTime")
            .addCategory(LIFECYCLE_CATEGORY)
            .addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongAccumulatorGauge> METRIC_KEY_HASH_DURATION_TIME = LongAccumulatorGauge.key(
                    "hashDurationTime")
            .addCategory(LIFECYCLE_CATEGORY)
            .addCategory(MAIN_CATEGORY);
    public static final MetricKey<LongCounter> METRIC_KEY_FLUSH_COUNT =
            LongCounter.key("flushes").addCategory(LIFECYCLE_CATEGORY).addCategory(MAIN_CATEGORY);

    @Override
    @NonNull
    public Collection<Metric.Builder<?, ?>> getMetricsToRegister() {
        ArrayList<Metric.Builder<?, ?>> builders = new ArrayList<>();

        builders.add(
                LongGauge.builder(METRIC_KEY_ENTITIES_COUNT).setDescription("Virtual map size as number of entries"));

        builders.add(LongCounter.builder(METRIC_KEY_ENTITIES_ADDED).setDescription("Added virtual map entities"));
        builders.add(LongCounter.builder(METRIC_KEY_ENTITIES_UPDATED).setDescription("Updated virtual map entities"));
        builders.add(LongCounter.builder(METRIC_KEY_ENTITIES_REMOVED).setDescription("Removed virtual map entities"));
        builders.add(LongCounter.builder(METRIC_KEY_ENTITIES_READ).setDescription("Read virtual map entities"));

        builders.add(LongGauge.builder(METRIC_KEY_NODE_CACHE_SIZE)
                .setDescription("Virtual node cache size in bytes")
                .setUnit(Unit.BYTE_UNIT));
        builders.add(LongGauge.builder(METRIC_KEY_PIPELINE_SIZE).setDescription("Virtual pipeline size"));
        builders.add(LongAccumulatorGauge.builder(METRIC_KEY_FLUSH_BACKPRESSURE_TIME, Long::sum)
                .setDescription("Virtual pipeline flush backpressure, ms")
                .setUnit(Unit.MILLISECOND_UNIT));
        builders.add(LongAccumulatorGauge.builder(METRIC_KEY_FAMILY_SIZE_BACKPRESSURE_TIME, Long::sum)
                .setDescription("Virtual pipeline family size backpressure, ms")
                .setUnit(Unit.MILLISECOND_UNIT));
        builders.add(LongAccumulatorGauge.builder(METRIC_KEY_MERGE_DURATION_TIME, Long::sum)
                .setDescription("Virtual root copy merge duration, ms")
                .setUnit(Unit.MILLISECOND_UNIT));
        builders.add(LongAccumulatorGauge.builder(METRIC_KEY_FLUSH_DURATION_TIME, Long::sum)
                .setDescription("Virtual root copy flush duration, ms")
                .setUnit(Unit.MILLISECOND_UNIT));
        builders.add(LongAccumulatorGauge.builder(METRIC_KEY_HASH_DURATION_TIME, Long::sum)
                .setDescription("Virtual root copy hash duration, ms")
                .setUnit(Unit.MILLISECOND_UNIT));
        builders.add(LongCounter.builder(METRIC_KEY_FLUSH_COUNT).setDescription("Virtual root copy flush count"));

        return builders;
    }
}
