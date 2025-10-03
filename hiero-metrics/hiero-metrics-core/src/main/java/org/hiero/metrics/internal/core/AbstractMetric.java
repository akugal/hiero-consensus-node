// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.export.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.datapoint.DataPointHolder;
import org.hiero.metrics.internal.export.SnapshotableMetric;
import org.hiero.metrics.internal.export.snapshot.UpdatableMetricSnapshot;

/**
 * Base class for all metric implementations requiring {@link Metric.Builder} for construction.
 */
public abstract class AbstractMetric<D, S extends DataPointSnapshot> implements SnapshotableMetric {

    private final MetricMetadata metadata;
    private final List<Label> constantLabels;
    private final List<String> dynamicLabelNames;

    protected final Map<LabelValues, DataPointHolder<D, S>> dataPoints;
    private final UpdatableMetricSnapshot<D, S> metricSnapshot;

    protected AbstractMetric(Builder<?, ?> builder) {
        metadata =
                new MetricMetadata(builder.type(), builder.key().name(), builder.getDescription(), builder.getUnit());

        constantLabels = builder.getConstantLabels().stream().sorted().toList();
        dynamicLabelNames = builder.getDynamicLabelNames().stream().sorted().toList();

        int dataPointsCapacity;
        if (dynamicLabelNames.isEmpty()) {
            dataPoints = null;
            dataPointsCapacity = 1;
        } else {
            dataPoints = new ConcurrentHashMap<>();
            dataPointsCapacity = 8;
        }
        metricSnapshot = new UpdatableMetricSnapshot<>(this, this::updateDatapointSnapshot, dataPointsCapacity);
    }

    protected final DataPointHolder<D, S> createDataPointHolder(D datapoint, LabelValues dynamicLabelValues) {
        DataPointHolder<D, S> dataPointHolder =
                new DataPointHolder<>(datapoint, createDataPointSnapshot(dynamicLabelValues));
        metricSnapshot.addDataPointHolder(dataPointHolder);
        return dataPointHolder;
    }

    protected abstract S createDataPointSnapshot(LabelValues dynamicLabelValues);

    protected abstract void updateDatapointSnapshot(DataPointHolder<D, S> dataPointHolder);

    protected LabelValues createLabelValues(String... namesAndValues) {
        Objects.requireNonNull(namesAndValues, "Label names and values must not be null");

        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Label names and values must be in pairs");
        }

        List<String> labelNames = dynamicLabelNames();

        if (namesAndValues.length / 2 != labelNames.size()) {
            throw new IllegalArgumentException(
                    "Expected " + labelNames.size() + " label names and values, got " + namesAndValues.length / 2);
        }

        if (labelNames.isEmpty()) {
            return LabelValues.empty();
        }

        for (int i = 0; i < labelNames.size(); i++) {
            String labelName = labelNames.get(i);

            int j = 2 * i;
            while (j < namesAndValues.length) {
                if (labelName.equals(namesAndValues[j])) {
                    if (namesAndValues[j + 1] == null) {
                        throw new IllegalArgumentException("Label value must not be null for label: " + labelName);
                    }
                    break;
                }
                j += 2;
            }

            if (j >= namesAndValues.length) {
                throw new IllegalArgumentException("Missing label name: " + labelName);
            }

            if (j > 2 * i) {
                // swap only if not already on it's place
                String tmpName = namesAndValues[2 * i];
                String tmpValue = namesAndValues[2 * i + 1];
                namesAndValues[2 * i] = namesAndValues[j];
                namesAndValues[2 * i + 1] = namesAndValues[j + 1];
                namesAndValues[j] = tmpName;
                namesAndValues[j + 1] = tmpValue;
            }
        }

        return new LabelNamesAndValues(namesAndValues);
    }

    @NonNull
    public final MetricMetadata metadata() {
        return metadata;
    }

    @NonNull
    @Override
    public final List<Label> constantLabels() {
        return constantLabels;
    }

    @NonNull
    @Override
    public final List<String> dynamicLabelNames() {
        return dynamicLabelNames;
    }

    @Override
    public final UpdatableMetricSnapshot<D, S> snapshot() {
        return metricSnapshot;
    }
}
