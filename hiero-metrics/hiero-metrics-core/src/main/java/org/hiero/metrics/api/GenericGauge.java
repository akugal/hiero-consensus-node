// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;
import org.hiero.metrics.api.utils.Unit;
import org.hiero.metrics.internal.DefaultGenericGauge;
import org.hiero.metrics.internal.datapoint.AtomicReferenceGaugeDataPoint;

public interface GenericGauge<T> extends StatefulMetric<GaugeDataPoint<T>>, GaugeDataPoint<T> {

    static <T> MetricKey<GenericGauge<T>> key(String name) {
        return MetricKey.of(name, GenericGauge.class);
    }

    static <T> MetricKey<GenericGauge<T>> key(String category, String name) {
        return MetricKey.of(category, name, GenericGauge.class);
    }

    static <T> Builder<T> builder(MetricKey<GenericGauge<T>> key, ToDoubleFunction<T> valueConverter) {
        return new Builder<>(key, valueConverter);
    }

    static Builder<Duration> durationBuilder(MetricKey<GenericGauge<Duration>> key, ChronoUnit unit) {
        return new Builder<>(
                        key,
                        duration -> ((double) duration.toNanos()
                                / unit.getDuration().toNanos()))
                .withUnit(Unit.getUnit(unit));
    }

    static <E extends Enum<E>> Builder<E> enumGauge(MetricKey<GenericGauge<E>> key) {
        return new Builder<>(key, Enum::ordinal);
    }

    final class Builder<T> extends StatefulMetric.Builder<GaugeDataPoint<T>, Builder<T>, GenericGauge<T>> {

        private Builder(MetricKey<GenericGauge<T>> key, ToDoubleFunction<T> valueConverter) {
            super(key, () -> new AtomicReferenceGaugeDataPoint<>(valueConverter));
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        @Override
        public GenericGauge<T> buildMetric() {
            return new DefaultGenericGauge<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
