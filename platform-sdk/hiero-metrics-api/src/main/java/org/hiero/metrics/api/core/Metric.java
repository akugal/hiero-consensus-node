// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public abstract class Metric {

    private final MetricMetadata metadata;
    protected final Label[] constantLabels;
    protected final String[] dynamicLabelNames;

    protected Metric(Builder<?, ?> builder) {
        metadata = new MetricMetadata(
                builder.getType(), builder.category, builder.name, builder.description, builder.unit);
        constantLabels = builder.constantLabels.values().toArray(new Label[0]);
        dynamicLabelNames = builder.dynamicLabelNames.toArray(new String[0]);
    }

    @NonNull
    public final MetricMetadata getMetadata() {
        return metadata;
    }

    public abstract void reset();

    @NonNull
    public abstract List<DataPointSnapshot> snapshot();

    @NonNull
    protected final DataPointSnapshot createSnapshot(
            double value, List<String> dynamicLabelValues, Label... additionalLabels) {
        return createSnapshot(value, 0, dynamicLabelValues, additionalLabels);
    }

    protected DataPointSnapshot createSnapshot(
            double value, long createdTimeMillis, List<String> dynamicLabelValues, Label... additionalLabels) {
        return new DataPointSnapshot(createdTimeMillis, value, mergeLabels(dynamicLabelValues, additionalLabels));
    }

    private List<Label> mergeLabels(List<String> dynamicLabelValues, Label... additionalLabels) {
        // support no dynamic label values even when dynamic labels declared
        if (dynamicLabelValues.isEmpty()) {
            if (additionalLabels.length == 0) {
                return List.of(constantLabels);
            } else {
                return List.of(additionalLabels);
            }
        }

        if (dynamicLabelValues.size() != dynamicLabelNames.length) {
            throw new IllegalStateException("Expected " + dynamicLabelNames.length + " label values, but got "
                    + dynamicLabelValues.size() + " for metric " + getMetadata().getFullName()
                    + " with dynamic labels: "
                    + Arrays.toString(dynamicLabelNames));
        }

        List<Label> labels =
                new ArrayList<>(constantLabels.length + dynamicLabelNames.length + additionalLabels.length);

        labels.addAll(Arrays.asList(constantLabels));

        for (int i = 0; i < dynamicLabelValues.size(); i++) {
            labels.add(new Label(dynamicLabelNames[i], dynamicLabelValues.get(i)));
        }

        labels.addAll(Arrays.asList(additionalLabels));

        return labels;
    }

    protected abstract static class Builder<B extends Builder<B, M>, M extends Metric> {

        protected final String name;

        private String category = "";
        private String description = "";
        private String unit = "";

        private final TreeMap<String, Label> constantLabels = new TreeMap<>();
        private final List<String> dynamicLabelNames = new ArrayList<>();

        protected Builder(String name) {
            this.name = ArgumentUtils.throwArgBlank(name, "name");
        }

        protected abstract MetricType getType();

        public B withCategory(String category) {
            this.category = ArgumentUtils.throwArgBlank(category, "category");
            return self();
        }

        public B withDescription(String description) {
            this.description = ArgumentUtils.throwArgBlank(description, "description");
            return self();
        }

        public B withUnit(String unit) {
            this.unit = ArgumentUtils.throwArgBlank(unit, "unit");
            return self();
        }

        public B withDynamicLabelNames(String... labelNames) {
            if (labelNames == null || labelNames.length == 0) {
                return self();
            }

            // verify no duplicates
            Set<String> lablesSet = Set.of(labelNames);
            dynamicLabelNames.addAll(lablesSet);

            return self();
        }

        public B withConstantLabel(Label label) {
            Objects.requireNonNull(label, "label must not be null");
            Label existingLabel = constantLabels.put(label.getName(), label);
            if (existingLabel != null && !existingLabel.equals(label)) {
                throw new IllegalArgumentException(label + " conflicts with existing: " + existingLabel);
            }
            return self();
        }

        public B withConstantLabels(Label... labels) {
            for (Label label : labels) {
                withConstantLabel(label);
            }
            return self();
        }

        public final M build() {
            for (String dynamicLabelName : dynamicLabelNames) {
                Label constLabel = constantLabels.get(dynamicLabelName);
                if (constLabel != null) {
                    throw new IllegalStateException("Dynamic label name '" + dynamicLabelName
                            + "' conflicts with a constant label: " + constLabel);
                }
            }
            return buildMetric();
        }

        public M register(MetricRegistry registry) {
            for (Label globalLabel : registry.getGlobalLabels()) {
                Label existing = constantLabels.put(globalLabel.getName(), globalLabel);
                if (existing != null) {
                    throw new IllegalStateException(
                            "Global label " + globalLabel + " conflicts with a constant label: " + existing);
                }
            }
            return registry.register(build());
        }

        public M register() {
            return register(MetricRegistry.getDefault());
        }

        protected abstract M buildMetric();

        protected abstract B self();
    }
}
