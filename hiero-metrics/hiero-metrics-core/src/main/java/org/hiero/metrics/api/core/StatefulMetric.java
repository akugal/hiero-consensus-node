// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.function.Function;

/**
 * Base interface for a metric that can have multiple data points holding values,
 * each associated with a unique set of dynamic label values.
 * <p>
 * Implementation is responsible for creating a new data point for each unique set of label values.
 * <p>
 * Clients should pay attention the dynamic label values cardinality, as high cardinality can lead to
 * higher costs for metrics backends. <b>Do not use</b> labels with values having unbounded cardinality,
 * such as IDs or timestamps.
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

    D getOrCreateLabeled(@NonNull String... namesAndValues);

    D getOrCreateLabeled(@NonNull I initializer, @NonNull String... namesAndValues);

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
