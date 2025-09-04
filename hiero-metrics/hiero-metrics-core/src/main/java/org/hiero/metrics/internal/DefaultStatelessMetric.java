// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.StatelessMetric;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractMetric;
import org.hiero.metrics.internal.core.SnapshotableMetric;

public final class DefaultStatelessMetric extends AbstractMetric implements StatelessMetric, SnapshotableMetric {

    private final Map<Map<String, String>, DoubleSupplier> labeledDataPoints = new ConcurrentHashMap<>();

    public DefaultStatelessMetric(StatelessMetric.Builder builder) {
        super(builder);

        for (Map.Entry<Map<String, String>, DoubleSupplier> entry :
                builder.getLabeledDataPoints().entrySet()) {
            registerDataPoint(entry.getValue(), entry.getKey());
        }
    }

    @NonNull
    @Override
    public List<DataPointSnapshot> snapshot() {
        List<DataPointSnapshot> snapshots = new ArrayList<>(labeledDataPoints.size());
        for (Map.Entry<Map<String, String>, DoubleSupplier> entry : labeledDataPoints.entrySet()) {
            snapshots.add(new DataPointSnapshot(
                    createDataPointLabels(entry.getKey()), entry.getValue().getAsDouble()));
        }
        return snapshots;
    }

    @NonNull
    @Override
    public StatelessMetric registerDataPoint(
            @NonNull DoubleSupplier valueSupplier, @NonNull Map<String, String> labels) {
        Objects.requireNonNull(valueSupplier, "Value supplier must not be null");

        verifyLabels(labels);

        if (labeledDataPoints.putIfAbsent(Map.copyOf(labels), valueSupplier) != null) {
            throw new IllegalArgumentException("A data point with the same label values already exists: " + labels);
        }

        return this;
    }
}
