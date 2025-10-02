// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
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
     * @param labelNamesAndValues labels as name followed by value
     * @return this metric
     * @throws IllegalArgumentException if a data point with the same label values already exists
     */
    @NonNull
    StatelessMetric registerDataPoint(@NonNull DoubleSupplier valueSupplier, @NonNull String... labelNamesAndValues);

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

        private final List<String[]> labelKeysAndValues = new ArrayList<>();
        private final List<DoubleSupplier> valuesSuppliers = new ArrayList<>();

        private Builder(MetricKey<StatelessMetric> key) {
            super(MetricType.GAUGE, key);
        }

        /**
         * Register a data point with the given value supplier and labels.
         * If a data point with the same label values already exists, an exception is thrown.
         *
         * @param valueSupplier the supplier to get the value of the data point
         * @param labelNamesAndValues pairs of label name followed by label value
         * @return this builder
         * @throws IllegalArgumentException if a data point with the same label values already exists
         */
        @NonNull
        public Builder registerDataPoint(
                @NonNull DoubleSupplier valueSupplier, @NonNull String... labelNamesAndValues) {
            // labels will be validated during metric construction
            valuesSuppliers.add(valueSupplier);
            labelKeysAndValues.add(labelNamesAndValues);
            return this;
        }

        public int getDataPointsSize() {
            return valuesSuppliers.size();
        }

        @NonNull
        public String[] getDataPointsLabelNamesAndValues(int idx) {
            return labelKeysAndValues.get(idx);
        }

        @NonNull
        public DoubleSupplier getValuesSupplier(int idx) {
            return valuesSuppliers.get(idx);
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
