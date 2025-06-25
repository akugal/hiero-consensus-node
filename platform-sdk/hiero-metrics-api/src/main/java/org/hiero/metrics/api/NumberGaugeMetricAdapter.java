// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;

public final class NumberGaugeMetricAdapter<D, V extends Number> extends StatefulMetric<D> implements Supplier<D> {

    private final Function<D, V> valueGetter;

    private NumberGaugeMetricAdapter(Builder<D, V> builder) {
        super(builder);

        valueGetter = Objects.requireNonNull(builder.valueGetter, "Value getter must not be null");
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
        return List.of(createSnapshot(value, PrimitiveDataType.DOUBLE, dynamicLabelValues));
    }

    public static class Builder<D, V extends Number>
            extends StatefulMetric.Builder<D, Builder<D, V>, NumberGaugeMetricAdapter<D, V>> {

        private Function<D, V> valueGetter;

        private Builder(String name) {
            super(name);
        }

        public Builder<D, V> withValueGetter(Function<D, V> valueGetter) {
            this.valueGetter = valueGetter;
            return this;
        }

        @Override
        public Builder<D, V> withContainerFactory(Supplier<D> valueContainerFactory) {
            return super.withContainerFactory(valueContainerFactory);
        }

        @Override
        protected NumberGaugeMetricAdapter<D, V> buildMetric() {
            return new NumberGaugeMetricAdapter<>(this);
        }

        @Override
        protected Builder<D, V> self() {
            return this;
        }
    }
}
