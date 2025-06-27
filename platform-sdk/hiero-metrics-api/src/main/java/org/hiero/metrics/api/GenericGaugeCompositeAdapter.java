// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.swirlds.base.ArgumentUtils;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;

public final class GenericGaugeCompositeAdapter<D> extends StatefulMetric<D> implements Supplier<D> {

    private final Label[] propertyLabels;
    private final PrimitiveDataType[] propertyTypes;
    private final Function<D, Object>[] propertyGetters;

    private GenericGaugeCompositeAdapter(Builder<D> builder) {
        super(builder);

        requireNonNull(builder.propertyLabel, "Property label must not be null");
        requireNonNull(builder.propertyNames, "Property names must not be null");

        propertyTypes = requireNonNull(builder.propertyTypes, "Property types must not be null").toArray(new PrimitiveDataType[0]);
        propertyGetters = requireNonNull(builder.propertyGetters, "Property getters must not be null")
                .toArray(new Function[0]);
        propertyLabels = builder.propertyNames.stream()
                .map(name -> new Label(builder.propertyLabel, name))
                .toArray(Label[]::new);

        if (propertyGetters.length != propertyLabels.length) {
            throw new IllegalStateException("Property names, types, and getters must have the same length for "
                    + getMetadata().getFullName());
        }
    }

    public static <D> Builder<D> builder(String name) {
        return new Builder<>(name);
    }

    @Override
    public D get() {
        return getNoLabels();
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(D datapoint, List<String> dynamicLabelValues) {
        List<DataPointSnapshot> snapshots = new ArrayList<>(propertyGetters.length);
        for (int i = 0; i < propertyGetters.length; i++) {
            Object value = propertyGetters[i].apply(datapoint);
            if (value != null) {
                snapshots.add(createSnapshot(
                        value,
                        propertyTypes[i],
                        dynamicLabelValues,
                        propertyLabels[i]));
            }
        }

        return snapshots;
    }

    public static class Builder<D> extends StatefulMetric.Builder<D, Builder<D>, GenericGaugeCompositeAdapter<D>> {

        private String propertyLabel;
        private final List<String> propertyNames = new ArrayList<>();
        private final List<PrimitiveDataType> propertyTypes = new ArrayList<>();
        private final List<Function<D, ?>> propertyGetters = new ArrayList<>();

        private Builder(String name) {
            super(name);
        }

        public Builder<D> withPropertyLabel(String propertyLabel) {
            this.propertyLabel = ArgumentUtils.throwArgBlank(propertyLabel, "propertyLabel");
            return this;
        }

        public <V> Builder<D> withProperty(String name, Class<V> valueType, Function<D, V> valueGetter) {
            propertyNames.add(ArgumentUtils.throwArgBlank(name, "propertyName"));
            propertyTypes.add(PrimitiveDataType.mapDataType(valueType));
            propertyGetters.add(Objects.requireNonNull(valueGetter, "Value getter must not be null"));
            return this;
        }

        @Override
        public Builder<D> withContainerFactory(Supplier<D> valueContainerFactory) {
            return super.withContainerFactory(valueContainerFactory);
        }

        @Override
        protected GenericGaugeCompositeAdapter<D> buildMetric() {
            if (new HashSet<>(propertyNames).size() != propertyGetters.size()) {
                throw new IllegalStateException("Property names must be unique");
            }

            return new GenericGaugeCompositeAdapter<>(this);
        }

        @Override
        protected Builder<D> self() {
            return this;
        }
    }
}
