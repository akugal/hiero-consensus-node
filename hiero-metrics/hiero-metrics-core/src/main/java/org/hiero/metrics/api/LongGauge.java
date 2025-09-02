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

/**
 * A stateful metric of type {@link MetricType#GAUGE} that holds {@link LongGaugeDataPoint} per label set.
 */
public interface LongGauge extends StatefulMetric<LongSupplier, LongGaugeDataPoint> {

    /**
     * Create a metric key for a {@link LongGauge} with the given name.
     *
     * @param name the name of the metric
     * @return the metric key
     */
    static MetricKey<LongGauge> key(String name) {
        return MetricKey.of(name, LongGauge.class);
    }

    /**
     * Create a builder for a {@link LongGauge} with the given metric key.
     *
     * @param key the metric key
     * @return the builder
     */
    static Builder builder(MetricKey<LongGauge> key) {
        return new Builder(key);
    }

    /**
     * Create a builder for a {@link LongGauge} with the given metric name.
     *
     * @param name the metric name
     * @return the builder
     */
    static Builder builder(String name) {
        return builder(key(name));
    }

    /**
     * Create a builder for a {@link LongGauge} with the given metric name that uses {@code sum}
     * as the aggregation operator.
     *
     * @param name            the metric name
     * @param resetOnExport if true, the gauge is reset to its initial value on export
     * @return the builder
     */
    static Builder sumBuilder(String name, boolean resetOnExport) {
        return builder(name).withOperator(StatUtils.LONG_SUM, resetOnExport);
    }

    /**
     * Create a builder for a {@link LongGauge} with the given metric name that uses {@code max}
     * as the aggregation operator and initial value of {@link Long#MIN_VALUE},
     * which won't be exported if not observed at least once.
     *
     * @param name            the metric name
     * @param resetOnExport   if true, the gauge is reset to its initial value on export
     * @return the builder
     */
    static Builder maxBuilder(String name, boolean resetOnExport) {
        return builder(name).withOperator(StatUtils.LONG_MAX, resetOnExport).withInitValue(Long.MIN_VALUE);
    }

    /**
     * Create a builder for a {@link LongGauge} with the given metric name that uses {@code min}
     * as the aggregation operator and initial value of {@link Long#MAX_VALUE},
     * which won't be exported if not observed at least once.
     *
     * @param name            the metric name
     * @param resetOnExport   if true, the gauge is reset to its initial value on export
     * @return the builder
     */
    static Builder minBuilder(String name, boolean resetOnExport) {
        return builder(name).withOperator(StatUtils.LONG_MIN, resetOnExport).withInitValue(Long.MAX_VALUE);
    }

    /**
     * A builder for a {@link LongGauge} using {@link AtomicLongGaugeDataPoint} or
     * {@link LongAccumulatorGaugeDataPoint} if operator is provided with {@link #withOperator(LongBinaryOperator, boolean)}.
     * Default initial value is <code>0L</code>.
     */
    final class Builder extends StatefulMetric.Builder<LongSupplier, LongGaugeDataPoint, Builder, LongGauge> {

        private LongBinaryOperator operator;
        private boolean resetOnExport = false;

        private Builder(MetricKey<LongGauge> key) {
            super(MetricType.GAUGE, key, StatUtils.LONG_INIT, AtomicLongGaugeDataPoint::new);
        }

        /**
         * @return {@code true} if the gauge is reset to its initial value after each export, {@code false} otherwise
         */
        public boolean isResetOnExport() {
            return resetOnExport;
        }

        /**
         * Set the initial value for the gauge and any data point within this metric.
         *
         * @param initValue the initial value for any data point within this metric
         * @return this builder
         */
        public Builder withInitValue(long initValue) {
            return withDefaultInitializer(StatUtils.asInitializer(initValue));
        }

        /**
         * Set the aggregation operator to use when updating the gauge value.
         * If not set, the gauge will simply hold the last value set.
         *
         * @param operator the aggregation operator, must not be {@code null}
         * @param resetOnExport if true, the gauge will be reset to its initial value after each export
         * @return this builder
         */
        public Builder withOperator(LongBinaryOperator operator, boolean resetOnExport) {
            this.operator = Objects.requireNonNull(operator, "Operator must not be null");
            this.resetOnExport = resetOnExport;
            return this;
        }

        /**
         * Build the {@link LongGauge} instance.
         *
         * @return the built metric
         */
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

        /**
         * @return this builder
         */
        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
