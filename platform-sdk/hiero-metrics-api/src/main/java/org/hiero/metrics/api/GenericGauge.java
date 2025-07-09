// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.core.Unit;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicReferenceGaugeDataPoint;

public final class GenericGauge<T> extends StatefulMetric<GaugeDataPoint<T>> implements GaugeDataPoint<T> {

    private GenericGauge(Builder<T> builder) {
        super(builder);
    }

    public static <T, V extends Number> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    public static Builder<Duration> durationBuilder(String name, ChronoUnit unit) {
        return new Builder<Duration>(name)
                .withUnit(Unit.getUnit(unit))
                .withValueConverter(duration -> ((double) duration.toNanos() / unit.getDuration().toNanos()));
    }

    public static <E extends Enum<E>> Builder<E> enumGauge(String name) {
        return new Builder<E>(name).withValueConverter(Enum::ordinal);
    }

    @Override
    protected void reset(GaugeDataPoint<T> dataPoint) {
        dataPoint.reset();
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(GaugeDataPoint<T> datapoint, List<String> dynamicLabelValues) {
        Number value = datapoint.get();
        if (value == null) {
            return List.of();
        }
        return List.of(createSnapshot(value.doubleValue(), dynamicLabelValues));
    }

    @Override
    public void update(T value) {
        getNoLabels().update(value);
    }

    @Override
    public Number get() {
        return getNoLabels().get();
    }

    public static class Builder<T>
            extends StatefulMetric.Builder<GaugeDataPoint<T>, Builder<T>, GenericGauge<T>> {

        private Builder(String name) {
            super(name);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
        }

        public Builder<T> withValueConverter(Function<T, Number> valueConverter) {
            Objects.requireNonNull(valueConverter, "ValueConverter must not be null");
            withContainerFactory(() -> new AtomicReferenceGaugeDataPoint<>(valueConverter));
            return this;
        }

        @Override
        public GenericGauge<T> buildMetric() {
            return new GenericGauge<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
