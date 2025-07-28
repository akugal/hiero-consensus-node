// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.stat.StatUtils.DOUBLE_INIT;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.internal.DefaultDoubleGauge;
import org.hiero.metrics.internal.datapoint.AtomicDoubleGaugeDataPoint;
import org.hiero.metrics.internal.datapoint.DoubleAccumulatorGaugeDataPoint;

public interface DoubleGauge extends StatefulMetric<DoubleGaugeDataPoint> {

    static MetricKey<DoubleGauge> key(String name) {
        return MetricKey.of(name, DoubleGauge.class);
    }

    static MetricKey<DoubleGauge> key(String category, String name) {
        return MetricKey.of(category, name, DoubleGauge.class);
    }

    static Builder builder(MetricKey<DoubleGauge> key) {
        return new Builder(key);
    }

    static Builder sumBuilder(MetricKey<DoubleGauge> key, boolean resetOnSnapshot) {
        return builder(key).withOperator(StatUtils.DOUBLE_SUM, resetOnSnapshot);
    }

    static Builder maxBuilder(MetricKey<DoubleGauge> key, boolean resetOnSnapshot) {
        return builder(key).withOperator(StatUtils.DOUBLE_MAX, resetOnSnapshot).withInitValue(Double.MIN_VALUE);
    }

    static Builder minBuilder(MetricKey<DoubleGauge> key, boolean resetOnSnapshot) {
        return builder(key).withOperator(StatUtils.DOUBLE_MIN, resetOnSnapshot).withInitValue(Double.MAX_VALUE);
    }

    final class Builder extends StatefulMetric.Builder<DoubleGaugeDataPoint, Builder, DoubleGauge> {

        private DoubleSupplier initializer = DOUBLE_INIT;
        private DoubleBinaryOperator operator;
        private boolean resetOnSnapshot = false;

        private Builder(MetricKey<DoubleGauge> key) {
            super(key, () -> new AtomicDoubleGaugeDataPoint(DOUBLE_INIT));
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        public boolean isResetOnSnapshot() {
            return resetOnSnapshot;
        }

        public Builder withInitializer(DoubleSupplier initializer) {
            this.initializer = Objects.requireNonNull(initializer, "Initializer must not be null");
            return this;
        }

        public Builder withInitValue(double initValue) {
            this.initializer = StatUtils.asInitializer(initValue);
            return this;
        }

        public Builder withOperator(DoubleBinaryOperator operator) {
            this.operator = Objects.requireNonNull(operator, "Operator must not be null");
            return this;
        }

        public Builder withOperator(DoubleBinaryOperator operator, boolean resetOnSnapshot) {
            withOperator(operator);
            this.resetOnSnapshot = resetOnSnapshot;
            return this;
        }

        @Override
        public DoubleGauge buildMetric() {
            if (operator != null) {
                withContainerFactory(() -> new DoubleAccumulatorGaugeDataPoint(operator, initializer));
            } else {
                withContainerFactory(() -> new AtomicDoubleGaugeDataPoint(initializer));
            }

            return new DefaultDoubleGauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
