// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

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

public interface GaugeAdapter<D> extends StatefulMetric<D> {

    static <D> MetricKey<GaugeAdapter<D>> key(String name) {
        return MetricKey.of(name, GaugeAdapter.class);
    }

    static <D> MetricKey<GaugeAdapter<D>> key(String category, String name) {
        return MetricKey.of(category, name, GaugeAdapter.class);
    }

    static <D> Builder<D> builder(
            MetricKey<GaugeAdapter<D>> key,
            @NonNull Supplier<D> dataPointFactory,
            @NonNull Function<D, Number> exportGetter) {
        return new Builder<>(key, dataPointFactory, exportGetter);
    }

    final class Builder<D> extends StatefulMetric.Builder<D, Builder<D>, GaugeAdapter<D>> {

        private final Function<D, Number> exportGetter;
        private Consumer<D> reset;

        private Builder(
                MetricKey<GaugeAdapter<D>> key,
                @NonNull Supplier<D> dataPointFactory,
                @NonNull Function<D, Number> exportGetter) {
            super(key, dataPointFactory);
            this.exportGetter = Objects.requireNonNull(exportGetter, "Export getter must not be null");
        }

        public Function<D, Number> getExportGetter() {
            return exportGetter;
        }

        @Nullable
        public Consumer<D> getReset() {
            return reset;
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        public Builder<D> withReset(Consumer<D> reset) {
            this.reset = Objects.requireNonNull(reset, "Value reset must not be null");
            return this;
        }

        @Override
        protected GaugeAdapter<D> buildMetric() {
            return new DefaultGaugeAdapter<>(this);
        }

        @Override
        protected Builder<D> self() {
            return this;
        }
    }
}
