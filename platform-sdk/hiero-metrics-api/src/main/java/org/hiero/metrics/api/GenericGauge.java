// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;
import org.hiero.metrics.api.utils.Unit;
import org.hiero.metrics.internal.DefaultGenericGauge;
import org.hiero.metrics.internal.datapoint.AtomicReferenceGaugeDataPoint;

public interface GenericGauge<T> extends StatefulMetric<GaugeDataPoint<T>>, GaugeDataPoint<T> {

    static <T> Builder<T> builder(String name, ToDoubleFunction<T> valueConverter) {
        return new Builder<>(name, valueConverter);
    }

    static Builder<Duration> durationBuilder(String name, ChronoUnit unit) {
        return new Builder<Duration>(
                        name,
                        duration -> ((double) duration.toNanos()
                                / unit.getDuration().toNanos()))
                .withUnit(Unit.getUnit(unit));
    }

    static <E extends Enum<E>> Builder<E> enumGauge(String name) {
        return new Builder<E>(name, Enum::ordinal);
    }

    final class Builder<T> extends StatefulMetric.Builder<GaugeDataPoint<T>, Builder<T>, GenericGauge<T>> {

        private Builder(String name, ToDoubleFunction<T> valueConverter) {
            super(name, () -> new AtomicReferenceGaugeDataPoint<>(valueConverter));
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
