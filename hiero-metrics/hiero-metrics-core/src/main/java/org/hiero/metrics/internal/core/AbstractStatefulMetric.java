// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.export.DataPointSnapshot;

public abstract class AbstractStatefulMetric<I, D> extends AbstractMetric
        implements StatefulMetric<I, D>, SnapshotableMetric {

    private final I defaultInitializer;
    private final Function<I, D> dataPointFactory;

    @Nullable
    private final D noLabelsDataPoint;

    private final Map<Map<String, String>, D> labeledDataPoints;

    protected AbstractStatefulMetric(StatefulMetric.Builder<I, D, ?, ?> builder) {
        super(builder);

        dataPointFactory = builder.getDataPointFactory();
        defaultInitializer = builder.getDefaultInitializer();

        if (dynamicLabelNames().isEmpty()) {
            noLabelsDataPoint = dataPointFactory.apply(defaultInitializer);
            labeledDataPoints = Map.of();
        } else {
            noLabelsDataPoint = null;
            labeledDataPoints = new ConcurrentHashMap<>();
        }
    }

    protected abstract void reset(D dataPoint);

    @Override
    public final void reset() {
        if (noLabelsDataPoint != null) {
            reset(noLabelsDataPoint);
        } else {
            labeledDataPoints.values().stream().parallel().forEach(this::reset);
        }
    }

    @NonNull
    @Override
    public final D getNotLabeled() {
        if (noLabelsDataPoint == null) {
            throw new IllegalStateException("This metric has dynamic labels, so you must call getOrCreateLabeled()");
        }
        return noLabelsDataPoint;
    }

    @NonNull
    @Override
    public D getOrCreateLabeled(@NonNull Map<String, String> labels) {
        return getOrCreateLabeled(labels, defaultInitializer);
    }

    @Override
    public D getOrCreateLabeled(@NonNull Map<String, String> labels, @NonNull I initializer) {
        if (noLabelsDataPoint != null) {
            if (labels != null && !labels.isEmpty()) {
                throw new IllegalArgumentException(getClass().getSimpleName()
                        + " "
                        + metadata().name()
                        + " was created without label names, so you must not provide label values.");
            }
            return noLabelsDataPoint;
        }

        Objects.requireNonNull(initializer);
        verifyLabels(labels);
        return labeledDataPoints.computeIfAbsent(Map.copyOf(labels), l -> dataPointFactory.apply(initializer));
    }

    @NonNull
    @Override
    public final List<DataPointSnapshot> snapshot() {
        if (noLabelsDataPoint != null) {
            List<DataPointSnapshot.ValueItem> valueItems = exportDataPoint(noLabelsDataPoint);
            if (valueItems.isEmpty()) {
                return List.of();
            } else {
                return List.of(new DataPointSnapshot(constantLabels(), valueItems));
            }
        } else if (labeledDataPoints.isEmpty()) {
            return List.of();
        } else {
            List<DataPointSnapshot> snapshots = new ArrayList<>(labeledDataPoints.size());
            for (Map.Entry<Map<String, String>, D> entry : labeledDataPoints.entrySet()) {
                List<DataPointSnapshot.ValueItem> valueItems = exportDataPoint(entry.getValue());
                if (!valueItems.isEmpty()) {
                    snapshots.add(new DataPointSnapshot(createDataPointLabels(entry.getKey()), valueItems));
                }
            }
            return snapshots;
        }
    }

    @NonNull
    protected abstract List<DataPointSnapshot.ValueItem> exportDataPoint(D datapoint);
}
