// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
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
                builder.getType(), builder.getKey().getName(), builder.getDescription(), builder.getUnit());

        constantLabels = List.copyOf(builder.getConstantLabels());
        dynamicLabelNames = List.copyOf(builder.getDynamicLabelNames());
        dynamicLabelNamesSet = Set.of(builder.getDynamicLabelNamesSet().toArray(new String[0]));
    }

    @NonNull
    public final MetricMetadata getMetadata() {
        return metadata;
    }

    @NonNull
    @Override
    public List<Label> getConstantLabels() {
        return constantLabels;
    }

    @NonNull
    @Override
    public List<String> getDynamicLabelNames() {
        return dynamicLabelNames;
    }

    protected Set<String> getDynamicLabelNamesSet() {
        return dynamicLabelNamesSet;
    }

    protected List<Label> createDataPointLabels(List<String> dynamicLabelValues) {
        if (dynamicLabelValues.size() != dynamicLabelNames.size()) {
            throw new IllegalStateException("Expected " + dynamicLabelNames.size() + " label values, but got "
                    + dynamicLabelValues.size() + " for metric " + getMetadata().getName()
                    + " with dynamic labels: "
                    + dynamicLabelNames);
        }

        if (constantLabels.isEmpty() && dynamicLabelValues.isEmpty()) {
            return List.of();
        }

        List<Label> labels = new ArrayList<>(constantLabels.size() + dynamicLabelNames.size());
        labels.addAll(constantLabels);
        for (int i = 0; i < dynamicLabelValues.size(); i++) {
            labels.add(new Label(dynamicLabelNames.get(i), dynamicLabelValues.get(i)));
        }
        return labels;
    }
}
