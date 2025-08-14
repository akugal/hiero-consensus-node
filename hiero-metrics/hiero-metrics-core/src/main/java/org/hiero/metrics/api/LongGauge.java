// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
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

public interface LongGauge extends StatefulMetric<LongSupplier, LongGaugeDataPoint> {

    static MetricKey<LongGauge> key(String name) {
        return MetricKey.of(name, LongGauge.class);
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

    final class Builder extends StatefulMetric.Builder<LongSupplier, LongGaugeDataPoint, Builder, LongGauge> {

        private LongBinaryOperator operator;
        private boolean resetOnExport = false;

        private Builder(MetricKey<LongGauge> key) {
            super(MetricType.GAUGE, key, StatUtils.LONG_INIT, AtomicLongGaugeDataPoint::new);
        }

        public boolean isResetOnExport() {
            return resetOnExport;
        }

        public Builder withInitValue(long initValue) {
            return withDefaultInitializer(StatUtils.asInitializer(initValue));
        }

        public Builder withOperator(LongBinaryOperator operator, boolean resetOnExport) {
            this.operator = Objects.requireNonNull(operator, "Operator must not be null");
            this.resetOnExport = resetOnExport;
            return this;
        }

        @NonNull
        @Override
        public LongGauge buildMetric() {
            if (operator != null) {
                withContainerFactory(init -> new LongAccumulatorGaugeDataPoint(operator, init));
            } else {
                withContainerFactory(AtomicLongGaugeDataPoint::new);
            }

            return new DefaultLongGauge(this);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
