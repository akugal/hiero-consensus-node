// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.stat.StatUtils.DEFAULT_STAT_LABEL;
import static org.hiero.metrics.api.stat.StatUtils.NO_DEFAULT_INITIALIZER;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.internal.DefaultStatsGaugeAdapter;

public interface StatsGaugeAdapter<I, D> extends StatefulMetric<I, D> {

    static <I, D> MetricKey<StatsGaugeAdapter<I, D>> key(String name) {
        return MetricKey.of(name, StatsGaugeAdapter.class);
    }

    static <I, D> MetricKey<StatsGaugeAdapter<I, D>> key(String category, String name) {
        return MetricKey.of(category, name, StatsGaugeAdapter.class);
    }

    static <I, D> Builder<I, D> builder(
            MetricKey<StatsGaugeAdapter<I, D>> key,
            @NonNull I defaultInitializer,
            @NonNull Function<I, D> dataPointFactory) {
        return new Builder<>(key, defaultInitializer, dataPointFactory);
    }

    static <D> Builder<Object, D> builder(
            MetricKey<StatsGaugeAdapter<Object, D>> key, @NonNull Supplier<D> dataPointFactory) {
        return new Builder<>(key, NO_DEFAULT_INITIALIZER, init -> dataPointFactory.get());
    }

    final class Builder<I, D> extends StatefulMetric.Builder<I, D, Builder<I, D>, StatsGaugeAdapter<I, D>> {

        private String statLabel = DEFAULT_STAT_LABEL;
        private final List<String> statNames = new ArrayList<>();
        private final List<Function<D, Number>> statExportGetters = new ArrayList<>();
        private Consumer<D> reset;

        private Builder(
                MetricKey<StatsGaugeAdapter<I, D>> key,
                @NonNull I defaultInitializer,
                @NonNull Function<I, D> dataPointFactory) {
            super(MetricType.GAUGE, key, defaultInitializer, dataPointFactory);
        }

        @NonNull
        public String getStatLabel() {
            return statLabel;
        }

        @NonNull
        public List<String> getStatNames() {
            return statNames;
        }

        @NonNull
        public List<Function<D, Number>> getStatExportGetters() {
            return statExportGetters;
        }

        @Nullable
        public Consumer<D> getReset() {
            return reset;
        }

        @NonNull
        public Builder<I, D> withReset(Consumer<D> reset) {
            this.reset = Objects.requireNonNull(reset, "Container stats reset must not be null");
            return this;
        }

        @NonNull
        public Builder<I, D> withStatLabel(String statLabel) {
            this.statLabel = ArgumentUtils.throwArgBlank(statLabel, "stat label");
            return this;
        }

        @NonNull
        public Builder<I, D> withStat(String statName, Function<D, Number> exportGetter) {
            statNames.add(ArgumentUtils.throwArgBlank(statName, "stat name"));
            statExportGetters.add(Objects.requireNonNull(exportGetter, "Export getter must not be null"));
            return this;
        }

        @NonNull
        @Override
        protected StatsGaugeAdapter<I, D> buildMetric() {
            if (statExportGetters.isEmpty()) {
                throw new IllegalStateException("At least one stat must be defined");
            }
            if (new HashSet<>(statNames).size() != statExportGetters.size()) {
                throw new IllegalStateException("Stat names must be unique");
            }

            if (constantLabels.containsKey(statLabel)) {
                throw new IllegalStateException("Stat label '" + statLabel + "' conflicts with a constant label");
            }
            for (String dynamicLabelName : getDynamicLabelNames()) {
                if (dynamicLabelName.equals(statLabel)) {
                    throw new IllegalStateException("Stat label '" + statLabel + "' conflicts with a dynamic label");
                }
            }

            return new DefaultStatsGaugeAdapter<>(this);
        }

        @NonNull
        @Override
        protected Builder<I, D> self() {
            return this;
        }
    }
}
