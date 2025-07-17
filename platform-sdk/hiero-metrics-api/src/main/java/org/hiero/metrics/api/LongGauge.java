// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.Objects;
import java.util.function.LongBinaryOperator;
import java.util.function.LongSupplier;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.utils.StatUtils;
import org.hiero.metrics.internal.DefaultLongGauge;
import org.hiero.metrics.internal.datapoint.AtomicLongGaugeDataPoint;
import org.hiero.metrics.internal.datapoint.LongAccumulatorGaugeDataPoint;

public interface LongGauge extends StatefulMetric<LongGaugeDataPoint>, LongGaugeDataPoint {

    static Builder builder(String name) {
        return new Builder(name);
    }

    static Builder sumBuilder(String name, boolean resetOnSnapshot) {
        return builder(name).withOperator(StatUtils.LONG_SUM, resetOnSnapshot);
    }

    static Builder maxBuilder(String name, boolean resetOnSnapshot) {
        return builder(name).withOperator(StatUtils.LONG_MAX, resetOnSnapshot).withInitValue(Long.MIN_VALUE);
    }

    static Builder minBuilder(String name, boolean resetOnSnapshot) {
        return builder(name).withOperator(StatUtils.LONG_MIN, resetOnSnapshot).withInitValue(Long.MAX_VALUE);
    }

    @Override
    void reset();

    final class Builder extends StatefulMetric.Builder<LongGaugeDataPoint, Builder, LongGauge> {

        private LongSupplier initializer = LongGaugeDataPoint.DEFAULT_INIT;
        private LongBinaryOperator operator;
        private boolean resetOnSnapshot = false;

        private Builder(String name) {
            super(name, () -> new AtomicLongGaugeDataPoint(LongGaugeDataPoint.DEFAULT_INIT));
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
            this.initializer = () -> initValue;
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
