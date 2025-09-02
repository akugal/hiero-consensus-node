// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.function.Function;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.StateSetDataPoint;
import org.hiero.metrics.internal.DefaultStateSet;
import org.hiero.metrics.internal.datapoint.EnumStateSetDataPoint;
import org.hiero.metrics.internal.datapoint.GenerictStateSetDataPoint;

/**
 * A stateful metric of type {@link MetricType#STATE_SET} that holds a set of states identified
 * by values of specified type.
 * @param <T> the type of the states in the set
 */
public interface StateSet<T> extends StatefulMetric<Map<T, Boolean>, StateSetDataPoint<T>> {

    /**
     * Create a metric key for a {@link StateSet} with the given name.
     *
     * @param name the name of the metric
     * @param <T>  the type of the states in the set
     * @return the metric key
     */
    static <T> MetricKey<StateSet<T>> key(String name) {
        return MetricKey.of(name, StateSet.class);
    }

    /**
     * Create a builder for a {@link StateSet} with the given metric key.
     *
     * @param key the metric key
     * @param <T> the type of the states in the set
     * @return the builder
     */
    static <T> Builder<T> builder(MetricKey<StateSet<T>> key) {
        return new Builder<>(key);
    }

    /**
     * Create a builder for a {@link StateSet} with the given metric name.
     *
     * @param name the metric name
     * @param <T>  the type of the states in the set
     * @return the builder
     */
    static <T> Builder<T> builder(String name) {
        return builder(key(name));
    }

    /**
     * Create a builder for a {@link StateSet} with the given metric name and enum class.
     * The states in the set will be of the specified enum type.
     *
     * @param name      the metric name
     * @param enumClass the enum class representing the states in the set
     * @param <E>       the type of the enum
     * @return the builder
     */
    static <E extends Enum<E>> Builder<E> enumBuilder(String name, Class<E> enumClass) {
        return new Builder<>(key(name), init -> new EnumStateSetDataPoint<>(init, enumClass));
    }

    /**
     * Builder for {@link StateSet} metrics using {@link GenerictStateSetDataPoint}.
     * By default, the initial state is empty and false for each state.
     * @param <T> the type of the states in the set
     */
    final class Builder<T>
            extends StatefulMetric.Builder<Map<T, Boolean>, StateSetDataPoint<T>, Builder<T>, StateSet<T>> {

        private Builder(@NonNull MetricKey<StateSet<T>> key) {
            this(key, GenerictStateSetDataPoint::new);
        }

        private Builder(
                @NonNull MetricKey<StateSet<T>> key, Function<Map<T, Boolean>, StateSetDataPoint<T>> dataPointFactory) {
            super(MetricType.STATE_SET, key, Map.of(), dataPointFactory);
        }

        /**
         * Build the {@link StateSet} metric.
         *
         * @return the built metric
         */
        @NonNull
        @Override
        protected StateSet<T> buildMetric() {
            withUnit(null); // StateSet does not have a unit

            // state set must not have a label as metric name
            for (String dynamicLabelName : getDynamicLabelNames()) {
                if (dynamicLabelName.equals(getKey().name())) {
                    throw new IllegalStateException(
                            "StateSet metric cannot have a dynamic label with the same name as the metric");
                }
            }

            return new DefaultStateSet<>(this);
        }

        /**
         * @return this builder
         */
        @NonNull
        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
