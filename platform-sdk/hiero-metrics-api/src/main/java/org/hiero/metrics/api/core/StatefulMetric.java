// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class StatefulMetric<D> extends Metric {

    private final Supplier<D> dataPointFactory;

    private volatile D noLabelsDataPoint;

    private final ConcurrentHashMap<List<String>, D> labeledDataPoints = new ConcurrentHashMap<>();

    protected StatefulMetric(Builder<D, ?, ?> builder) {
        super(builder);

        dataPointFactory = Objects.requireNonNull(builder.valueContainerFactory, "Data point factory must not be null");
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

    @Override
    public final List<DataPointSnapshot> snapshot() {
        if (labeledDataPoints.isEmpty()) {
            return createSnapshots(noLabelsDataPoint, List.of());
        }

        List<DataPointSnapshot> snapshots = new ArrayList<>(labeledDataPoints.size() + 1);
        snapshots.addAll(createSnapshots(noLabelsDataPoint, List.of()));

        for (Map.Entry<List<String>, D> entry : labeledDataPoints.entrySet()) {
            snapshots.addAll(createSnapshots(entry.getValue(), entry.getKey()));
        }

        return snapshots;
    }

    protected final D getNoLabels() {
        if (noLabelsDataPoint == null) {
            synchronized (this) {
                if (noLabelsDataPoint == null) {
                    noLabelsDataPoint = dataPointFactory.get();
                }
            }
        }
        return noLabelsDataPoint;
    }

    protected abstract List<DataPointSnapshot> createSnapshots(D datapoint, List<String> dynamicLabelValues);

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
