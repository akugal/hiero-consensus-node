package org.hiero.metrics.api;

import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.core.StatUtils;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicLongGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongAccumulatorGaugeDataPoint;

import java.util.List;
import java.util.Objects;
import java.util.function.LongBinaryOperator;
import java.util.function.LongSupplier;
import java.util.function.ToLongFunction;

public final class LongGauge
        extends StatefulMetric<LongGaugeDataPoint>
        implements LongGaugeDataPoint {

    private final ToLongFunction<LongGaugeDataPoint> snapshotValueSupplier;

    private LongGauge(Builder builder) {
        super(builder);

        snapshotValueSupplier = Objects.requireNonNull(builder.snapshotValueSupplier, "Snapshot value supplier must not be null");
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static Builder sumBuilder(String name) {
        return builder(name)
                .withOperator(StatUtils.LONG_SUM);
    }

    public static Builder maxBuilder(String name) {
        return builder(name)
                .withOperator(StatUtils.LONG_MAX)
                .withInitValue(Long.MIN_VALUE);
    }

    public static Builder minBuilder(String name) {
        return builder(name)
                .withOperator(StatUtils.LONG_MIN)
                .withInitValue(Long.MAX_VALUE);
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(LongGaugeDataPoint datapoint, List<String> dynamicLabelValues) {
        long value = snapshotValueSupplier.applyAsLong(datapoint);
        if (Long.MAX_VALUE == value || Long.MIN_VALUE == value) {
            // This is a safeguard against using long extreme values as a valid metric value.
            // MAX_VALUE or MIN_VALUE could be initial values for min or max statistics,
            // but they should not be reported as actual metric values.
            return List.of();
        }
        return List.of(createSnapshot(value, PrimitiveDataType.LONG, dynamicLabelValues));
    }

    @Override
    public long getInitValue() {
        return getNoLabels().getInitValue();
    }

    @Override
    public void update(long value) {
        getNoLabels().update(value);
    }

    @Override
    public long getAndReset() {
        return getNoLabels().getAndReset();
    }

    @Override
    public long getAsLong() {
        return getNoLabels().getAsLong();
    }

    public static class Builder extends StatefulMetric.Builder<LongGaugeDataPoint, Builder, LongGauge> {

        private LongSupplier initializer = () -> 0L;
        private LongBinaryOperator operator;
        private ToLongFunction<LongGaugeDataPoint> snapshotValueSupplier = LongSupplier::getAsLong;

        private Builder(String name) {
            super(name);
        }

        public Builder withInitializer(LongSupplier initializer) {
            this.initializer = Objects.requireNonNull(initializer, "Initializer must not be null");
            return this;
        }

        public Builder withInitValue(long initValue) {
            this.initializer = () -> initValue;
            return this;
        }

        public Builder withOperator(LongBinaryOperator operator) {
            this.operator = Objects.requireNonNull(operator, "Operator must not be null");
            return this;
        }

        public Builder resetOnSnapshot() {
            snapshotValueSupplier = LongGaugeDataPoint::getAndReset;
            return this;
        }

        @Override
        public LongGauge buildMetric() {
            if (operator != null) {
                withContainerFactory(() -> new LongAccumulatorGaugeDataPoint(operator, initializer));
            } else {
                withContainerFactory(() -> new AtomicLongGaugeDataPoint(initializer));
            }

            return new LongGauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
