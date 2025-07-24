// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.utils.StatUtils.DEFAULT_STAT_LABEL;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
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

public interface StatsGaugeAdapter<D> extends StatefulMetric<D>, Supplier<D> {

    static <D> MetricKey<StatsGaugeAdapter<D>> key(String name) {
        return MetricKey.of(name, StatsGaugeAdapter.class);
    }

    static <D> MetricKey<StatsGaugeAdapter<D>> key(String category, String name) {
        return MetricKey.of(category, name, StatsGaugeAdapter.class);
    }

    static <D> Builder<D> builder(MetricKey<StatsGaugeAdapter<D>> key, @NonNull Supplier<D> valueContainerFactory) {
        return new Builder<>(key, valueContainerFactory);
    }

    final class Builder<D> extends StatefulMetric.Builder<D, Builder<D>, StatsGaugeAdapter<D>> {

        private String statLabel = DEFAULT_STAT_LABEL;
        private final List<String> statNames = new ArrayList<>();
        private final List<Function<D, Number>> statSnapshotGetters = new ArrayList<>();
        private Consumer<D> reset;

        private Builder(MetricKey<StatsGaugeAdapter<D>> key, @NonNull Supplier<D> valueContainerFactory) {
            super(key, Objects.requireNonNull(valueContainerFactory, "container factory must not be null"));
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        public String getStatLabel() {
            return statLabel;
        }

        public List<String> getStatNames() {
            return statNames;
        }

        public List<Function<D, Number>> getStatSnapshotGetters() {
            return statSnapshotGetters;
        }

        public Consumer<D> getReset() {
            return reset;
        }

        public Builder<D> withReset(Consumer<D> reset) {
            this.reset = Objects.requireNonNull(reset, "Container stats reset must not be null");
            return this;
        }

        public Builder<D> withStatLabel(String statLabel) {
            this.statLabel = ArgumentUtils.throwArgBlank(statLabel, "stat label");
            return this;
        }

        public Builder<D> withStat(String statName, Function<D, Number> snapshotValueGetter) {
            statNames.add(ArgumentUtils.throwArgBlank(statName, "stat name"));
            statSnapshotGetters.add(
                    Objects.requireNonNull(snapshotValueGetter, "Snapshot value getter must not be null"));
            return this;
        }

        @Override
        protected StatsGaugeAdapter<D> buildMetric() {
            if (statSnapshotGetters.isEmpty()) {
                throw new IllegalStateException("At least one stat must be defined");
            }
            if (new HashSet<>(statNames).size() != statSnapshotGetters.size()) {
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

        @Override
        protected Builder<D> self() {
            return this;
        }
    }
}
