// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicReferenceGaugeDataPoint;

public final class GenericGauge<T, V> extends StatefulMetric<GaugeDataPoint<T, V>> implements GaugeDataPoint<T, V> {

    private final PrimitiveDataType dataType;

    private GenericGauge(Builder<T, V> builder) {
        super(builder);

        dataType = Objects.requireNonNull(builder.dataType, "Data type must not be null");
    }

    public static <T, V> Builder<T, V> builder(String name) {
        return new Builder<>(name);
    }

    public static Builder<String, String> infoBuilder(String name) {
        return new Builder<String, String>(name).withValueConverter(String.class, Function.identity());
    }

    public static <E extends Enum<E>> Builder<E, String> enumBuilder(String name) {
        return new Builder<E, String>(name).withValueConverter(String.class, Enum::name);
    }

    public static Builder<Duration, Double> durationBuilder(String name, ChronoUnit unit) {
        // TODO automatically map time unit to metric unit
        return new Builder<Duration, Double>(name).withValueConverter(Double.class, duration ->
                (double) (duration.toNanos() / unit.getDuration().toNanos()));
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(GaugeDataPoint<T, V> datapoint, List<String> dynamicLabelValues) {
        return List.of(createSnapshot(datapoint.get(), dataType, dynamicLabelValues));
    }

    @Override
    public void update(T value) {
        getNoLabels().update(value);
    }

    @Override
    public V get() {
        return getNoLabels().get();
    }

    public static class Builder<T, V>
            extends StatefulMetric.Builder<GaugeDataPoint<T, V>, Builder<T, V>, GenericGauge<T, V>> {

        private PrimitiveDataType dataType;

        private Builder(String name) {
            super(name);
        }

        public Builder<T, V> withValueConverter(Class<V> valueType, Function<T, V> valueConverter) {
            dataType = PrimitiveDataType.mapDataType(valueType);
            withContainerFactory(() -> new AtomicReferenceGaugeDataPoint<>(valueConverter));
            return this;
        }

        @Override
        public GenericGauge<T, V> buildMetric() {
            return new GenericGauge<>(this);
        }

        @Override
        protected Builder<T, V> self() {
            return this;
        }
    }
}
