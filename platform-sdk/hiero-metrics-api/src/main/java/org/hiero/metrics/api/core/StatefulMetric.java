// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.snapshot.DataPointSnapshot;

public abstract class StatefulMetric<D> extends Metric {

    private final Supplier<D> dataPointFactory;

    @Nullable
    private final D noLabelsDataPoint;

    private final ConcurrentHashMap<List<String>, D> labeledDataPoints = new ConcurrentHashMap<>();

    protected StatefulMetric(Builder<D, ?, ?> builder) {
        super(builder);

        dataPointFactory = Objects.requireNonNull(builder.valueContainerFactory, "Data point factory must not be null");

        if (dynamicLabelNames.length == 0) {
            noLabelsDataPoint = dataPointFactory.get();
        } else {
            noLabelsDataPoint = null;
        }
    }

    protected abstract void reset(D dataPoint);

    @Override
    public final void reset() {
        if (noLabelsDataPoint != null) {
            reset(noLabelsDataPoint);
        }

        labeledDataPoints.values().stream().parallel().forEach(this::reset);
    }

    public final D getOrCreateLabeled(String... labelValues) {
        if (labelValues.length != dynamicLabelNames.length) {
            if (labelValues.length == 0) {
                throw new IllegalArgumentException(getClass().getSimpleName()
                        + " "
                        + getMetadata().getFullName()
                        + " was created with label names, so you must call labelValues(...)"
                        + " when using it.");
            } else {
                throw new IllegalArgumentException(
                        "Expected " + dynamicLabelNames.length + " label values, but got " + labelValues.length + ".");
            }
        }

        checkNoNullLabels(labelValues);

        return labeledDataPoints.computeIfAbsent(Arrays.asList(labelValues), labels -> dataPointFactory.get());
    }

    @NonNull
    @Override
    public final List<DataPointSnapshot> snapshotDataPoints() {
        if (noLabelsDataPoint != null) {
            List<DataPointSnapshot.ValueItem> valueItems = snapshotDataPoint(noLabelsDataPoint);
            if (valueItems.isEmpty()) {
                return List.of();
            } else {
                return List.of(new DataPointSnapshot(List.of(), valueItems));
            }
        } else if (labeledDataPoints.isEmpty()) {
            return List.of();
        } else {
            List<DataPointSnapshot> snapshots = new ArrayList<>(labeledDataPoints.size());
            for (Map.Entry<List<String>, D> entry : labeledDataPoints.entrySet()) {
                List<DataPointSnapshot.ValueItem> valueItems = snapshotDataPoint(entry.getValue());
                if (!valueItems.isEmpty()) {
                    snapshots.add(new DataPointSnapshot(createDataPointLabels(entry.getKey()), valueItems));
                }
            }
            return snapshots;
        }
    }

    @NonNull
    protected final D getNoLabels() {
        if (noLabelsDataPoint == null) {
            throw new IllegalStateException("This metric has dynamic labels, so you must call getOrCreateLabeled()");
        }
        return noLabelsDataPoint;
    }

    @NonNull
    protected abstract List<DataPointSnapshot.ValueItem> snapshotDataPoint(D datapoint);

    private void checkNoNullLabels(String[] labelValues) {
        for (int i = 0; i < labelValues.length; i++) {
            if (labelValues[i] == null) {
                throw new IllegalArgumentException("null label value for metric "
                        + getMetadata().getFullName()
                        + " and label "
                        + dynamicLabelNames[i]);
            }
        }
    }

    protected abstract static class Builder<D, B extends Builder<D, B, M>, M extends StatefulMetric<D>>
            extends Metric.Builder<B, M> {

        private Supplier<D> valueContainerFactory;

        protected Builder(String name) {
            super(name);
        }

        protected B withContainerFactory(Supplier<D> valueContainerFactory) {
            this.valueContainerFactory =
                    Objects.requireNonNull(valueContainerFactory, "Value container factory must not be null");
            return self();
        }
    }
}
