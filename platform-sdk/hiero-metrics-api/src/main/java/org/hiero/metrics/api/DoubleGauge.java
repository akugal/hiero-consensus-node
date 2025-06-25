// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.List;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatUtils;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicDoubleGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.DoubleAccumulatorGaugeDataPoint;

public final class DoubleGauge extends StatefulMetric<DoubleGaugeDataPoint> implements DoubleGaugeDataPoint {

    private final ToDoubleFunction<DoubleGaugeDataPoint> snapshotValueSupplier;

    private DoubleGauge(Builder builder) {
        super(builder);

        snapshotValueSupplier = builder.snapshotValueSupplier;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static Builder sumBuilder(String name) {
        return builder(name).withOperator(StatUtils.DOUBLE_SUM);
    }

    public static Builder maxBuilder(String name) {
        return builder(name).withOperator(StatUtils.DOUBLE_MAX).withInitValue(Double.MIN_VALUE);
    }

    public static Builder minBuilder(String name) {
        return builder(name).withOperator(StatUtils.DOUBLE_MIN).withInitValue(Double.MAX_VALUE);
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(DoubleGaugeDataPoint datapoint, List<String> dynamicLabelValues) {
        double value = snapshotValueSupplier.applyAsDouble(datapoint);
        if (Double.MAX_VALUE == value || Double.MIN_VALUE == value) {
            // This is a safeguard against using double extreme values as a valid metric value.
            // MAX_VALUE or MIN_VALUE could be initial values for min or max statistics,
            // but they should not be reported as actual metric values.
            return List.of();
        }
        return List.of(createSnapshot(value, PrimitiveDataType.DOUBLE, dynamicLabelValues));
    }

    @Override
    public double getInitValue() {
        return getNoLabels().getInitValue();
    }

    @Override
    public void update(double value) {
        getNoLabels().update(value);
    }

    @Override
    public double getAndReset() {
        return getNoLabels().getAndReset();
    }

    @Override
    public double getAsDouble() {
        return getNoLabels().getAsDouble();
    }

    public static class Builder extends StatefulMetric.Builder<DoubleGaugeDataPoint, Builder, DoubleGauge> {

        private DoubleSupplier initializer = DoubleGaugeDataPoint.DEFAULT_INIT;
        private DoubleBinaryOperator operator;
        private ToDoubleFunction<DoubleGaugeDataPoint> snapshotValueSupplier = DoubleSupplier::getAsDouble;

        private Builder(String name) {
            super(name);
        }

        public Builder withInitializer(DoubleSupplier initializer) {
            this.initializer = Objects.requireNonNull(initializer, "Initializer must not be null");
            return this;
        }

        public Builder withInitValue(double initValue) {
            this.initializer = () -> initValue;
            return this;
        }

        public Builder withOperator(DoubleBinaryOperator operator) {
            this.operator = Objects.requireNonNull(operator, "Operator must not be null");
            return this;
        }

        public Builder withResetOnSnapshot() {
            snapshotValueSupplier = DoubleGaugeDataPoint::getAndReset;
            return this;
        }

        @Override
        public DoubleGauge buildMetric() {
            if (operator != null) {
                withContainerFactory(() -> new DoubleAccumulatorGaugeDataPoint(operator, initializer));
            } else {
                withContainerFactory(() -> new AtomicDoubleGaugeDataPoint(initializer));
            }

            return new DoubleGauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
