// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import static org.hiero.metrics.api.core.MetricUtils.EMPTY_LABELS;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import org.hiero.metrics.api.core.snapshot.DataPointSnapshot;

public abstract class Metric {

    private final MetricMetadata metadata;
    protected final Label[] constantLabels;
    protected final String[] dynamicLabelNames;

    protected Metric(Builder<?, ?> builder) {
        metadata = new MetricMetadata(
                builder.getType(), builder.category, builder.name, builder.description, builder.unit);

        if (builder.globalLabels.isEmpty() && builder.constantLabels.isEmpty()) {
            constantLabels = EMPTY_LABELS;
        } else {
            // combine global labels and constant labels
            constantLabels = new Label[builder.constantLabels.size() + builder.globalLabels.size()];
            int i = 0;
            for (Label globalLabel : builder.globalLabels) {
                constantLabels[i++] = globalLabel;
            }
            for (Label constantLabel : builder.constantLabels.values()) {
                constantLabels[i++] = constantLabel;
            }
        }

        dynamicLabelNames = builder.dynamicLabelNames.toArray(new String[0]);
    }

    @NonNull
    public final MetricMetadata getMetadata() {
        return metadata;
    }

    public abstract void reset();

    @NonNull
    protected abstract List<DataPointSnapshot> snapshotDataPoints();

    protected List<Label> createDataPointLabels(List<String> dynamicLabelValues) {
        if (dynamicLabelValues.size() != dynamicLabelNames.length) {
            throw new IllegalStateException("Expected " + dynamicLabelNames.length + " label values, but got "
                    + dynamicLabelValues.size() + " for metric " + getMetadata().getFullName()
                    + " with dynamic labels: "
                    + Arrays.toString(dynamicLabelNames));
        }

        List<Label> labels = new ArrayList<>(constantLabels.length + dynamicLabelNames.length);
        labels.addAll(Arrays.asList(constantLabels));
        for (int i = 0; i < dynamicLabelValues.size(); i++) {
            labels.add(new Label(dynamicLabelNames[i], dynamicLabelValues.get(i)));
        }
        return labels;
    }

    protected abstract static class Builder<B extends Builder<B, M>, M extends Metric> {

        protected final String name;

        private String category;
        private String description;
        private String unit;

        private List<Label> globalLabels = List.of(); // will be set if registered in the registry
        private final TreeMap<String, Label> constantLabels = new TreeMap<>();
        private final List<String> dynamicLabelNames = new ArrayList<>();

        protected Builder(String name) {
            this.name = ArgumentUtils.throwArgBlank(name, "name");
        }

        protected abstract MetricType getType();

        public final B withCategory(String category) {
            this.category = ArgumentUtils.throwArgBlank(category, "category");
            return self();
        }

        public final B withDescription(String description) {
            this.description = ArgumentUtils.throwArgBlank(description, "description");
            return self();
        }

        public final B withUnit(String unit) {
            this.unit = ArgumentUtils.throwArgBlank(unit, "unit");
            return self();
        }

        public final B withDynamicLabelNames(String... labelNames) {
            if (labelNames == null || labelNames.length == 0) {
                return self();
            }

            // verify no duplicates
            Set<String> lablesSet = Set.of(labelNames);
            dynamicLabelNames.addAll(lablesSet);

            return self();
        }

        public final B withConstantLabel(Label label) {
            Objects.requireNonNull(label, "label must not be null");
            Label existingLabel = constantLabels.put(label.getName(), label);
            if (existingLabel != null && !existingLabel.equals(label)) {
                throw new IllegalArgumentException(label + " conflicts with existing: " + existingLabel);
            }
            return self();
        }

        public final B withConstantLabels(Label... labels) {
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

        public final M register(MetricRegistry registry) {
            globalLabels = registry.getGlobalLabels();
            for (Label globalLabel : globalLabels) {
                Label existing = constantLabels.get(globalLabel.getName());
                if (existing != null) {
                    throw new IllegalStateException(
                            "Global label " + globalLabel + " conflicts with a constant label: " + existing);
                }
            }
            return registry.register(build());
        }

        public final M register() {
            return register(MetricRegistry.getDefault());
        }

        protected abstract M buildMetric();

        protected abstract B self();
    }
}
