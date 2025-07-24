// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public interface Metric {

    @NonNull
    MetricMetadata getMetadata();

    @NonNull
    List<Label> getConstantLabels();

    @NonNull
    List<String> getDynamicLabelNames();

    void reset();

    abstract class Builder<B extends Builder<B, M>, M extends Metric> {

        private final MetricKey<M> key;
        private String description;
        private String unit;

        protected final TreeMap<String, Label> constantLabels = new TreeMap<>();
        private final List<String> dynamicLabelNames = new ArrayList<>();
        private final Set<String> dynamicLabelNamesSet = new HashSet<>();

        protected Builder(@NonNull MetricKey<M> key) {
            this.key = Objects.requireNonNull(key, "key must not be null");
        }

        public abstract MetricType getType();

        public MetricKey<M> getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public String getUnit() {
            return unit;
        }

        public Collection<Label> getConstantLabels() {
            return constantLabels.values();
        }

        public List<String> getDynamicLabelNames() {
            return dynamicLabelNames;
        }

        public Set<String> getDynamicLabelNamesSet() {
            return dynamicLabelNamesSet;
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
            for (String labelName : labelNames) {
                if (dynamicLabelNamesSet.add(labelName)) {
                    dynamicLabelNames.add(labelName);
                }
            }

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

        public final B withConstantLabels(Collection<Label> labels) {
            for (Label label : labels) {
                withConstantLabel(label);
            }
            return self();
        }

        public final B withConstantLabels(Label... labels) {
            return withConstantLabels(Arrays.asList(labels));
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
            return registry.register(this);
        }

        protected abstract M buildMetric();

        protected abstract B self();
    }
}
