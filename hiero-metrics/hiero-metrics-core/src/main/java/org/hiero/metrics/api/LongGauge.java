// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.stat.StatUtils.LONG_INIT;

import java.util.Objects;
import java.util.function.LongBinaryOperator;
import java.util.function.LongSupplier;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.internal.DefaultLongGauge;
import org.hiero.metrics.internal.datapoint.AtomicLongGaugeDataPoint;
import org.hiero.metrics.internal.datapoint.LongAccumulatorGaugeDataPoint;

public interface LongGauge extends StatefulMetric<LongGaugeDataPoint> {

    static MetricKey<LongGauge> key(String name) {
        return MetricKey.of(name, LongGauge.class);
    }

    static MetricKey<LongGauge> key(String category, String name) {
        return MetricKey.of(category, name, LongGauge.class);
    }

    static Builder builder(MetricKey<LongGauge> key) {
        return new Builder(key);
    }

    static Builder sumBuilder(MetricKey<LongGauge> key, boolean resetOnSnapshot) {
        return builder(key).withOperator(StatUtils.LONG_SUM, resetOnSnapshot);
    }

    static Builder maxBuilder(MetricKey<LongGauge> key, boolean resetOnSnapshot) {
        return builder(key).withOperator(StatUtils.LONG_MAX, resetOnSnapshot).withInitValue(Long.MIN_VALUE);
    }

    static Builder minBuilder(MetricKey<LongGauge> key, boolean resetOnSnapshot) {
        return builder(key).withOperator(StatUtils.LONG_MIN, resetOnSnapshot).withInitValue(Long.MAX_VALUE);
    }

    final class Builder extends StatefulMetric.Builder<LongGaugeDataPoint, Builder, LongGauge> {

        private LongSupplier initializer = LONG_INIT;
        private LongBinaryOperator operator;
        private boolean resetOnSnapshot = false;

        private Builder(MetricKey<LongGauge> key) {
            super(key, () -> new AtomicLongGaugeDataPoint(LONG_INIT));
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        public boolean isResetOnSnapshot() {
            return resetOnSnapshot;
        }

        public Builder withInitializer(LongSupplier initializer) {
            this.initializer = Objects.requireNonNull(initializer, "Initializer must not be null");
            return this;
        }

        public Builder withInitValue(long initValue) {
            this.initializer = StatUtils.asInitializer(initValue);
            return this;
        }

        public Builder withOperator(LongBinaryOperator operator, boolean resetOnSnapshot) {
            this.operator = Objects.requireNonNull(operator, "Operator must not be null");
            this.resetOnSnapshot = resetOnSnapshot;
            return this;
        }

        @Override
        public LongGauge buildMetric() {
            if (operator != null) {
                withContainerFactory(() -> new LongAccumulatorGaugeDataPoint(operator, initializer));
            } else {
                withContainerFactory(() -> new AtomicLongGaugeDataPoint(initializer));
            }

            return new DefaultLongGauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
