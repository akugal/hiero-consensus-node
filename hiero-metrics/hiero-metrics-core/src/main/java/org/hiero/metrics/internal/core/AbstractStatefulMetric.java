// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.export.DataPointSnapshot;

public abstract class AbstractStatefulMetric<D> extends AbstractMetric
        implements StatefulMetric<D>, SnapshotableMetric {

    private final Supplier<D> dataPointFactory;

    @Nullable
    private final D noLabelsDataPoint;

    private final Map<List<String>, D> labeledDataPoints;

    protected AbstractStatefulMetric(StatefulMetric.Builder<D, ?, ?> builder) {
        super(builder);

        dataPointFactory = builder.getValueContainerFactory();

        if (getDynamicLabelNames().isEmpty()) {
            noLabelsDataPoint = dataPointFactory.get();
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
    public D getOrCreateLabeled(Map<String, String> labels) {
        if (noLabelsDataPoint != null) {
            if (!labels.isEmpty()) {
                throw new IllegalArgumentException(getClass().getSimpleName()
                        + " "
                        + getMetadata().getName()
                        + " was created without label names, so you must not provide label values.");
            }
            return noLabelsDataPoint;
        } else if (labels.size() != getDynamicLabelNames().size()) {
            throw new IllegalArgumentException(
                    "Expected different size of labels. Expected: + " + getDynamicLabelNames() + ", got " + labels);
        } else if (labels.keySet().equals(getDynamicLabelNamesSet())) {
            throw new IllegalArgumentException(
                    "Expected different label names. Expected: + " + getDynamicLabelNames() + ", got " + labels);
        }

        List<String> labelValues = new ArrayList<>(getDynamicLabelNames().size());
        for (String labelName : getDynamicLabelNames()) {
            labelValues.add(labels.get(labelName));
        }

        return labeledDataPoints.computeIfAbsent(labelValues, l -> dataPointFactory.get());
    }

    @NonNull
    public final D getOrCreateLabeled(String... labelValues) {
        if (noLabelsDataPoint != null) {
            if (labelValues.length != 0) {
                throw new IllegalArgumentException(getClass().getSimpleName()
                        + " "
                        + getMetadata().getName()
                        + " was created without label names, so you must not provide label values.");
            }
            return noLabelsDataPoint;
        } else if (labelValues.length != getDynamicLabelNames().size()) {
            throw new IllegalArgumentException("Expected different size of labels. Expected: + "
                    + getDynamicLabelNames() + ", got " + Arrays.asList(labelValues));
        }

        checkNoNullLabels(labelValues);

        return labeledDataPoints.computeIfAbsent(Arrays.asList(labelValues), labels -> dataPointFactory.get());
    }

    @NonNull
    @Override
    public final List<DataPointSnapshot> snapshot() {
        if (noLabelsDataPoint != null) {
            List<DataPointSnapshot.ValueItem> valueItems = snapshotDataPoint(noLabelsDataPoint);
            if (valueItems.isEmpty()) {
                return List.of();
            } else {
                return List.of(new DataPointSnapshot(getConstantLabels(), valueItems));
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
    protected abstract List<DataPointSnapshot.ValueItem> snapshotDataPoint(D datapoint);

    private void checkNoNullLabels(String[] labelValues) {
        for (int i = 0; i < labelValues.length; i++) {
            if (labelValues[i] == null) {
                throw new IllegalArgumentException("null label value for metric "
                        + getMetadata().getName()
                        + " and label "
                        + getDynamicLabelNames().get(i));
            }
        }
    }
}
