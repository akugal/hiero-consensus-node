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

public interface StateSet<T> extends StatefulMetric<Map<T, Boolean>, StateSetDataPoint<T>> {

    static <T> MetricKey<StateSet<T>> key(String name) {
        return MetricKey.of(name, StateSet.class);
    }

    static <T> MetricKey<StateSet<T>> key(String category, String name) {
        return MetricKey.of(category, name, StateSet.class);
    }

    static <T> Builder<T> builder(MetricKey<StateSet<T>> key) {
        return new Builder<>(key);
    }

    static <E extends Enum<E>> Builder<E> enumBuilder(MetricKey<StateSet<E>> key, Class<E> enumClass) {
        return new Builder<>(key, init -> new EnumStateSetDataPoint<>(init, enumClass));
    }

    @Override
    default void reset() {
        // No reset operation defined for StateSet, as it is not meaningful to reset the entire set.
        // Individual states can be set to true or false, but the set itself does not have a reset state.
    }

    final class Builder<T>
            extends StatefulMetric.Builder<Map<T, Boolean>, StateSetDataPoint<T>, Builder<T>, StateSet<T>> {

        private Builder(@NonNull MetricKey<StateSet<T>> key) {
            this(key, GenerictStateSetDataPoint::new);
        }

        private Builder(
                @NonNull MetricKey<StateSet<T>> key, Function<Map<T, Boolean>, StateSetDataPoint<T>> dataPointFactory) {
            super(MetricType.STATE_SET, key, Map.of(), dataPointFactory);
        }

        @NonNull
        @Override
        protected StateSet<T> buildMetric() {
            withUnit(null); // StateSet does not have a unit

            // state set must not have a label as metric name
            for (String dynamicLabelName : getDynamicLabelNames()) {
                if (dynamicLabelName.equals(getKey().getName())) {
                    throw new IllegalStateException(
                            "StateSet metric cannot have a dynamic label with the same name as the metric");
                }
            }

            return new DefaultStateSet<>(this);
        }

        @NonNull
        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
