// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricMetadata;

/**
 * Base class for all metric implementations requiring {@link Metric.Builder} for construction.
 */
public abstract class AbstractMetric implements Metric {

    private final MetricMetadata metadata;
    private final List<Label> constantLabels;
    private final List<String> dynamicLabelNames;

    protected AbstractMetric(Builder<?, ?> builder) {
        metadata =
                new MetricMetadata(builder.type(), builder.key().name(), builder.getDescription(), builder.getUnit());

        constantLabels = builder.getConstantLabels().stream().sorted().toList();
        dynamicLabelNames = builder.getDynamicLabelNames().stream().sorted().toList();
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

    /**
     * Verify that the provided labels map has the correct label names and non-null keys and values.
     *
     * @param labels labels for the dynamic label names as map
     * @throws IllegalArgumentException if the label names do not match the expected dynamic label names
     * @throws NullPointerException if any label key or value is null
     */
    protected void verifyLabels(@NonNull Map<String, String> labels) {
        Objects.requireNonNull(labels, "Labels map must not be null");

        // check label names match registered ones
        if (!labels.keySet().equals(dynamicLabelNames)) {
            throw new IllegalArgumentException(
                    "Expected different label names. Expected: + " + dynamicLabelNames + ", got " + labels);
        }

        // Check no null keys and values
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
     * <p>
     * All labels wil consist of constant labels sorted by name followed by dynamic labels sorted by name.
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
