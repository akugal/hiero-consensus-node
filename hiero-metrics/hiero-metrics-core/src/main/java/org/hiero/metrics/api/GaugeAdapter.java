// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.stat.StatUtils.NO_DEFAULT_INITIALIZER;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.internal.DefaultGaugeAdapter;

public interface GaugeAdapter<I, D> extends StatefulMetric<I, D> {

    static <I, D> MetricKey<GaugeAdapter<I, D>> key(String name) {
        return MetricKey.of(name, GaugeAdapter.class);
    }

    static <I, D> MetricKey<GaugeAdapter<I, D>> key(String category, String name) {
        return MetricKey.of(category, name, GaugeAdapter.class);
    }

    static <I, D> Builder<I, D> builder(
            MetricKey<GaugeAdapter<I, D>> key,
            @NonNull I defaultInitializer,
            @NonNull Function<I, D> dataPointFactory,
            @NonNull Function<D, Number> exportGetter) {
        return new Builder<>(key, defaultInitializer, dataPointFactory, exportGetter);
    }

    static <D> Builder<Object, D> builder(
            MetricKey<GaugeAdapter<Object, D>> key,
            @NonNull Supplier<D> dataPointFactory,
            @NonNull Function<D, Number> exportGetter) {
        return new Builder<>(key, NO_DEFAULT_INITIALIZER, init -> dataPointFactory.get(), exportGetter);
    }

    final class Builder<I, D> extends StatefulMetric.Builder<I, D, Builder<I, D>, GaugeAdapter<I, D>> {

        private final Function<D, Number> exportGetter;
        private Consumer<D> reset;

        private Builder(
                @NonNull MetricKey<GaugeAdapter<I, D>> key,
                @NonNull I defaultInitializer,
                @NonNull Function<I, D> dataPointFactory,
                @NonNull Function<D, Number> exportGetter) {
            super(MetricType.GAUGE, key, defaultInitializer, dataPointFactory);
            this.exportGetter = Objects.requireNonNull(exportGetter, "Export getter must not be null");
        }

        @NonNull
        public Function<D, Number> getExportGetter() {
            return exportGetter;
        }

        @Nullable
        public Consumer<D> getReset() {
            return reset;
        }

        @NonNull
        public Builder<I, D> withReset(Consumer<D> reset) {
            this.reset = Objects.requireNonNull(reset, "Value reset must not be null");
            return this;
        }

        @NonNull
        @Override
        protected GaugeAdapter<I, D> buildMetric() {
            return new DefaultGaugeAdapter<>(this);
        }

        @NonNull
        @Override
        protected Builder<I, D> self() {
            return this;
        }
    }
}
