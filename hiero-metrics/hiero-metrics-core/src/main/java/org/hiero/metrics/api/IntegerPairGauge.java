// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.utils.StatUtils.INT_AVERAGE;
import static org.hiero.metrics.api.utils.StatUtils.INT_SUM;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import java.util.function.ToDoubleBiFunction;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.IntegerPairDataPoint;
import org.hiero.metrics.internal.DefaultIntegerPairGauge;
import org.hiero.metrics.internal.datapoint.AtomicIntegerPairDataPoint;

public interface IntegerPairGauge extends StatefulMetric<IntegerPairDataPoint>, IntegerPairDataPoint {

    static MetricKey<IntegerPairGauge> key(String name) {
        return MetricKey.of(name, IntegerPairGauge.class);
    }

    static Builder builder(
            @NonNull MetricKey<IntegerPairGauge> key, @NonNull ToDoubleBiFunction<Integer, Integer> resulFunction) {
        return new Builder(key, resulFunction);
    }

    static Builder averageBuilder(@NonNull MetricKey<IntegerPairGauge> key) {
        return new Builder(key, INT_AVERAGE);
    }

    final class Builder extends StatefulMetric.Builder<IntegerPairDataPoint, Builder, IntegerPairGauge> {

        private final ToDoubleBiFunction<Integer, Integer> resulFunction;
        private IntBinaryOperator leftAccumulator = INT_SUM;
        private IntBinaryOperator rightAccumulator = INT_SUM;
        private boolean resetOnSnapshot = false;

        private Builder(
                @NonNull MetricKey<IntegerPairGauge> key, @NonNull ToDoubleBiFunction<Integer, Integer> resulFunction) {
            super(key, () -> new AtomicIntegerPairDataPoint(INT_SUM, INT_SUM, resulFunction));
            this.resulFunction = Objects.requireNonNull(resulFunction, "result function must not be null");
        }

        @NonNull
        public IntBinaryOperator getLeftAccumulator() {
            return leftAccumulator;
        }

        @NonNull
        public IntBinaryOperator getRightAccumulator() {
            return rightAccumulator;
        }

        @NonNull
        public ToDoubleBiFunction<Integer, Integer> getResulFunction() {
            return resulFunction;
        }

        public boolean isResetOnSnapshot() {
            return resetOnSnapshot;
        }

        @NonNull
        public Builder withLeftAccumulator(@NonNull IntBinaryOperator leftAccumulator) {
            this.leftAccumulator = Objects.requireNonNull(leftAccumulator, "left accumulator must not be null");
            return this;
        }

        @NonNull
        public Builder withRightAccumulator(@NonNull IntBinaryOperator rightAccumulator) {
            this.rightAccumulator = Objects.requireNonNull(rightAccumulator, "right accumulator must not be null");
            return this;
        }

        @NonNull
        public Builder withResetOnSnapshot() {
            this.resetOnSnapshot = true;
            return this;
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        @Override
        public IntegerPairGauge buildMetric() {
            withContainerFactory(
                    () -> new AtomicIntegerPairDataPoint(leftAccumulator, rightAccumulator, resulFunction));
            return new DefaultIntegerPairGauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
