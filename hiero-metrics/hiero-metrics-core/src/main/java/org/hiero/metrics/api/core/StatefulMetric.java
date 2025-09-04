// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base interface for a metric that can have multiple data points holding values,
 * each associated with a unique set of dynamic label values.
 * <p>
 * Implementation is responsible for creating a new data point for each unique set of label values.
 *
 * @param <I> the type of the initializer used to create new data points per label set
 * @param <D> the type of the data point
 */
public interface StatefulMetric<I, D> extends Metric {

    /**
     * Get the data point with no labels.
     *
     * @return the data point with no labels
     * @throws IllegalStateException if metric has dynamic labels specified during creation
     */
    @NonNull
    D getNotLabeled();

    /**
     * Get or create a data point with the given label values.
     * If a data point with the same label values already exists, it is returned.
     * Otherwise, a new data point is created using the default initializer.
     *
     * @param labels the map of labels. It is highly recommended to use {@code Map.of(...)}>, since in most cases the
     *               number of labels is small, and it returns immutable map.
     * @return the data point with the given label values
     * @throws IllegalArgumentException if the number of labels does not match the number of dynamic labels
     * @throws NullPointerException if labels is null or contains null keys or values
     */
    @NonNull
    D getOrCreateLabeled(@NonNull Map<String, String> labels);

    /**
     * Get or create a data point with the given label values.
     * If a data point with the same label values already exists, it is returned.
     * Otherwise, a new data point is created using the given initializer.
     *
     * @param labels the label values
     * @param initializer the initializer to use to create a new data point if one does not already exist
     * @return the data point with the given label values
     * @throws IllegalArgumentException if the number of labels does not match the number of dynamic labels
     * @throws NullPointerException if labels or initializer is null or contains null keys or values
     */
    D getOrCreateLabeled(@NonNull Map<String, String> labels, @NonNull I initializer);

    /**
     * Base abstract builder for {@link StatefulMetric}.
     *
     * @param <I> the type of the initializer used to create new data points per label set
     * @param <D> the type of the data point
     * @param <B> the type of the builder
     * @param <M> the type of the metric
     */
    abstract class Builder<I, D, B extends Builder<I, D, B, M>, M extends StatefulMetric<I, D>>
            extends Metric.Builder<B, M> {

        private I defaultInitializer;
        private Function<I, D> dataPointFactory;

        /**
         * Constructor for a stateful metric builder.
         *
         * @param type               the metric type, must not be {@code null}
         * @param key                the metric key, must not be {@code null}
         * @param defaultInitializer the default initializer to use to create new data points, must not be {@code null}
         * @param dataPointFactory   the factory function to create new data points, must not be {@code null}
         */
        protected Builder(
                @NonNull MetricType type,
                @NonNull MetricKey<M> key,
                @NonNull I defaultInitializer,
                @NonNull Function<I, D> dataPointFactory) {
            super(type, key);
            withDefaultInitializer(defaultInitializer);
            withContainerFactory(dataPointFactory);
        }

        /**
         * @return the data point factory, never {@code null}
         */
        @NonNull
        public Function<I, D> getDataPointFactory() {
            return dataPointFactory;
        }

        /**
         * @return the default initializer, never {@code null}
         */
        @NonNull
        public I getDefaultInitializer() {
            return defaultInitializer;
        }

        /**
         * Set the default initializer to use to create new data points.
         *
         * @param defaultInitializer the default initializer, must not be {@code null}
         * @return this builder
         */
        @NonNull
        public final B withDefaultInitializer(@NonNull I defaultInitializer) {
            this.defaultInitializer =
                    Objects.requireNonNull(defaultInitializer, "Default initializer must not be null");
            return self();
        }

        /**
         * Set the factory function to use to create new data points.
         *
         * @param dataPointFactory the factory function, must not be {@code null}
         * @return this builder
         */
        @NonNull
        protected B withContainerFactory(Function<I, D> dataPointFactory) {
            this.dataPointFactory = Objects.requireNonNull(dataPointFactory, "Data point factory must not be null");
            return self();
        }
    }
}
