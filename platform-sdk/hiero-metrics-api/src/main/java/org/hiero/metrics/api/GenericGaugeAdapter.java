// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;

public final class GenericGaugeAdapter<D, V> extends StatefulMetric<D> implements Supplier<D> {

    private final Function<D, V> valueGetter;
    private final PrimitiveDataType valueDataType;

    private GenericGaugeAdapter(Builder<D, V> builder) {
        super(builder);

        valueGetter = Objects.requireNonNull(builder.valueGetter, "Value getter must not be null");
        valueDataType = PrimitiveDataType.mapDataType(builder.valueType);
    }

    public static <D, V extends Number> Builder<D, V> builder(String name) {
        return new Builder<>(name);
    }

    @Override
    public D get() {
        return getNoLabels();
    }

    public V getValue() {
        return valueGetter.apply(getNoLabels());
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(D datapoint, List<String> dynamicLabelValues) {
        V value = valueGetter.apply(datapoint);
        if (value == null) {
            return List.of();
        }
        return List.of(createSnapshot(value, valueDataType, dynamicLabelValues));
    }

    public static class Builder<D, V>
            extends StatefulMetric.Builder<D, Builder<D, V>, GenericGaugeAdapter<D, V>> {

        private Class<V> valueType;
        private Function<D, V> valueGetter;

        private Builder(String name) {
            super(name);
        }

        public Builder<D, V> withValueGetter(Class<V> valueType, Function<D, V> valueGetter) {
            this.valueType = Objects.requireNonNull(valueType, "Value class must not be null");
            this.valueGetter = Objects.requireNonNull(valueGetter, "Value getter must not be null");
            return this;
        }

        @Override
        public Builder<D, V> withContainerFactory(Supplier<D> valueContainerFactory) {
            return super.withContainerFactory(valueContainerFactory);
        }

        @Override
        protected GenericGaugeAdapter<D, V> buildMetric() {
            return new GenericGaugeAdapter<>(this);
        }

        @Override
        protected Builder<D, V> self() {
            return this;
        }
    }
}
