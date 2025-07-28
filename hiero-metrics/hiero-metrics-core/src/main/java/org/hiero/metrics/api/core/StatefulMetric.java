// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public interface StatefulMetric<D> extends Metric {

    @NonNull
    D getOrCreateLabeled(Map<String, String> labels);

    @NonNull
    D getOrCreateLabeled(String... labelValues);

    abstract class Builder<D, B extends Builder<D, B, M>, M extends StatefulMetric<D>> extends Metric.Builder<B, M> {

        private Supplier<D> valueContainerFactory;

        protected Builder(@NonNull MetricKey<M> key, @NonNull Supplier<D> valueContainerFactory) {
            super(key);
            withContainerFactory(valueContainerFactory);
        }

        @NonNull
        public Supplier<D> getValueContainerFactory() {
            return valueContainerFactory;
        }

        public B withContainerFactory(Supplier<D> valueContainerFactory) {
            this.valueContainerFactory =
                    Objects.requireNonNull(valueContainerFactory, "Value container factory must not be null");
            return self();
        }
    }
}
