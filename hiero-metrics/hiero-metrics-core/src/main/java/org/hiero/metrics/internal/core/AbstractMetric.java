// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricMetadata;

public abstract class AbstractMetric implements Metric {

    private final MetricMetadata metadata;
    private final List<Label> constantLabels;
    private final List<String> dynamicLabelNames;
    private final Set<String> dynamicLabelNamesSet;

    protected AbstractMetric(Builder<?, ?> builder) {
        metadata = new MetricMetadata(
                builder.getType(), builder.getKey().name(), builder.getDescription(), builder.getUnit());

        constantLabels = List.copyOf(builder.getConstantLabels());
        dynamicLabelNames = List.copyOf(builder.getDynamicLabelNames());
        dynamicLabelNamesSet = Set.of(builder.getDynamicLabelNamesSet().toArray(new String[0]));
    }

    @NonNull
    public final MetricMetadata metadata() {
        return metadata;
    }

    @NonNull
    @Override
    public List<Label> constantLabels() {
        return constantLabels;
    }

    @NonNull
    @Override
    public List<String> dynamicLabelNames() {
        return dynamicLabelNames;
    }

    protected Set<String> getDynamicLabelNamesSet() {
        return dynamicLabelNamesSet;
    }

    /**
     * Verify that the provided labels map has the correct label names and non-null keys and values.
     *
     * @param labels labels for the dynamic label names as map
     * @throws IllegalArgumentException if the label names do not match the expected dynamic label names
     * @throws NullPointerException if any label key or value is null
     */
    protected void verifyLabels(Map<String, String> labels) {
        Objects.requireNonNull(labels);
        if (!labels.keySet().equals(getDynamicLabelNamesSet())) {
            throw new IllegalArgumentException(
                    "Expected different label names. Expected: + " + dynamicLabelNames() + ", got " + labels);
        }

        for (Map.Entry<String, String> entry : labels.entrySet()) {
            if (entry.getKey() == null) {
                throw new NullPointerException("Label key cannot be null");
            }
            if (entry.getValue() == null) {
                throw new NullPointerException("Label value cannot be null: " + entry.getKey());
            }
        }
    }

    /**
     * Create the full list of labels for a data point, combining constant and dynamic labels.
     *
     * @param labels labels for the dynamic label names as map
     * @return the full mutable list of labels for the data point
     */
    protected List<Label> createDataPointLabels(Map<String, String> labels) {
        if (constantLabels.isEmpty() && labels.isEmpty()) {
            return List.of();
        }

        final List<Label> labelsList = new ArrayList<>(constantLabels.size() + dynamicLabelNames.size());
        labelsList.addAll(constantLabels);
        for (String dynamicLabelName : dynamicLabelNames) {
            labelsList.add(new Label(dynamicLabelName, labels.get(dynamicLabelName)));
        }
        return labelsList;
    }
}
