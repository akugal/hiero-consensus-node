// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public interface StatefulMetric<I, D> extends Metric {

    @NonNull
    D getNotLabeled();

    @NonNull
    D getOrCreateLabeled(Map<String, String> labels);

    D getOrCreateLabeled(Map<String, String> labels, I initializer);

    @NonNull
    D getOrCreateLabeled(String... labelValues);

    abstract class Builder<I, D, B extends Builder<I, D, B, M>, M extends StatefulMetric<I, D>>
            extends Metric.Builder<B, M> {

        private I defaultInitializer;
        private Function<I, D> dataPointFactory;

        protected Builder(
                @NonNull MetricType type,
                @NonNull MetricKey<M> key,
                @NonNull I defaultInitializer,
                @NonNull Function<I, D> dataPointFactory) {
            super(type, key);
            withDefaultInitializer(defaultInitializer);
            withContainerFactory(dataPointFactory);
        }

        @NonNull
        public Function<I, D> getDataPointFactory() {
            return dataPointFactory;
        }

        @NonNull
        public I getDefaultInitializer() {
            return defaultInitializer;
        }

        @NonNull
        public final B withDefaultInitializer(@NonNull I defaultInitializer) {
            this.defaultInitializer =
                    Objects.requireNonNull(defaultInitializer, "Default initializer must not be null");
            return self();
        }

        @NonNull
        protected B withContainerFactory(Function<I, D> dataPointFactory) {
            this.dataPointFactory = Objects.requireNonNull(dataPointFactory, "Data point factory must not be null");
            return self();
        }
    }
}
