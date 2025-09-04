// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.internal.DefaultStatelessMetric;

/**
 * A stateless metric of type {@link MetricType#GAUGE} that doesn't hold any state
 * and gets its value using provided suppliers.
 */
public interface StatelessMetric extends Metric {

    /**
     * Create a metric key for a {@link StatelessMetric} with the given name.
     *
     * @param name the name of the metric
     * @return the metric key
     */
    @NonNull
    static MetricKey<StatelessMetric> key(@NonNull String name) {
        return MetricKey.of(name, StatelessMetric.class);
    }

    /**
     * Create a builder for a {@link StatelessMetric} with the given metric key.
     *
     * @param key the metric key
     * @return the builder
     */
    @NonNull
    static Builder builder(@NonNull MetricKey<StatelessMetric> key) {
        return new Builder(key);
    }

    /**
     * Create a builder for a {@link StatelessMetric} with the given metric name.
     *
     * @param name the metric name
     * @return the builder
     */
    @NonNull
    static Builder builder(@NonNull String name) {
        return builder(key(name));
    }

    /**
     * Register a data point with the given value supplier and labels.
     * If a data point with the same label values already exists, an exception is thrown.
     *
     * @param valueSupplier the supplier to get the value of the data point
     * @param labels        the labels for the data point
     * @return this metric
     * @throws IllegalArgumentException if a data point with the same label values already exists
     */
    @NonNull
    StatelessMetric registerDataPoint(@NonNull DoubleSupplier valueSupplier, @NonNull Map<String, String> labels);

    /**
     * Stateless metrics do not hold any state, so this is a no-op.
     */
    @Override
    default void reset() {
        // no op
    }

    /**
     * Builder for {@link StatelessMetric}.
     */
    final class Builder extends Metric.Builder<Builder, StatelessMetric> {

        private final Map<Map<String, String>, DoubleSupplier> labeledDataPoints = new HashMap<>();

        private Builder(MetricKey<StatelessMetric> key) {
            super(MetricType.GAUGE, key);
        }

        /**
         * Register a data point with the given value supplier and labels.
         * If a data point with the same label values already exists, an exception is thrown.
         *
         * @param valueSupplier the supplier to get the value of the data point
         * @param labels        the labels for the data point
         * @return this builder
         * @throws IllegalArgumentException if a data point with the same label values already exists
         */
        @NonNull
        public Builder registerDataPoint(@NonNull DoubleSupplier valueSupplier, @NonNull Map<String, String> labels) {
            // labels will be validated during metric construction
            if (labeledDataPoints.put(labels, valueSupplier) != null) {
                throw new IllegalArgumentException("A data point with the same label values already exists: " + labels);
            }
            return this;
        }

        /**
         * Get the map of label sets to their corresponding value suppliers.
         *
         * @return the map of label sets to value suppliers
         */
        @NonNull
        public Map<Map<String, String>, DoubleSupplier> getLabeledDataPoints() {
            return labeledDataPoints;
        }

        /**
         * Build the {@link StatelessMetric} instance.
         *
         * @return the built metric
         */
        @NonNull
        @Override
        protected StatelessMetric buildMetric() {
            return new DefaultStatelessMetric(this);
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
