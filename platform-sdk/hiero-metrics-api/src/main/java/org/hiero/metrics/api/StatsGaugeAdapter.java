// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static java.util.Objects.requireNonNull;
import static org.hiero.metrics.api.core.StatUtils.DEFAULT_STAT_LABEL;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.core.snapshot.DataPointSnapshot;

public final class StatsGaugeAdapter<D> extends StatefulMetric<D> implements Supplier<D> {

    private final Label[] statLabels;
    private final Function<D, Number>[] statSnapshotGetters;
    private final Consumer<D> reset;

    @SuppressWarnings("unchecked")
    private StatsGaugeAdapter(Builder<D> builder) {
        super(builder);

        requireNonNull(builder.statNames, "Property names must not be null");

        reset = builder.reset != null ? builder.reset : container -> {}; // no-op reset if no specified
        statSnapshotGetters = builder.statSnapshotGetters.toArray(new Function[0]);
        statLabels = new Label[builder.statNames.size()];
        for (int i = 0; i < statLabels.length; i++) {
            statLabels[i] = new Label(builder.statLabel, builder.statNames.get(i));
        }
    }

    public static <D> Builder<D> builder(String name) {
        return new Builder<>(name);
    }

    @Override
    public D get() {
        return getNoLabels();
    }

    @Override
    protected void reset(D dataPoint) {
        reset.accept(dataPoint);
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(D datapoint) {
        List<DataPointSnapshot.ValueItem> valueItems = new ArrayList<>(statSnapshotGetters.length);
        for (int i = 0; i < statSnapshotGetters.length; i++) {
            Number value = statSnapshotGetters[i].apply(datapoint);
            if (value != null) {
                valueItems.add(new DataPointSnapshot.ValueItem(value.doubleValue(), statLabels[i]));
            }
        }

        return valueItems;
    }

    public static class Builder<D> extends StatefulMetric.Builder<D, Builder<D>, StatsGaugeAdapter<D>> {

        private String statLabel = DEFAULT_STAT_LABEL;
        private final List<String> statNames = new ArrayList<>();
        private final List<Function<D, Number>> statSnapshotGetters = new ArrayList<>();
        private Consumer<D> reset;

        private Builder(String name) {
            super(name);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
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
        public Builder<D> withContainerFactory(Supplier<D> valueContainerFactory) {
            return super.withContainerFactory(valueContainerFactory);
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
            for (String dynamicLabelName : dynamicLabelNames) {
                if (dynamicLabelName.equals(statLabel)) {
                    throw new IllegalStateException("Stat label '" + statLabel + "' conflicts with a dynamic label");
                }
            }

            return new StatsGaugeAdapter<>(this);
        }

        @Override
        protected Builder<D> self() {
            return this;
        }
    }
}
