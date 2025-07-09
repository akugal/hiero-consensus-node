// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;

public final class GaugeAdapter<D> extends StatefulMetric<D> implements Supplier<D> {

    private final Function<D, Number> snapshotGetter;
    private final Consumer<D> reset;

    private GaugeAdapter(Builder<D> builder) {
        super(builder);

        snapshotGetter = Objects.requireNonNull(builder.snapshotGetter, "Snapshot getter must not be null");
        reset = builder.reset != null ? builder.reset : container -> {};
    }

    public static <D, V extends Number> Builder<D> builder(String name) {
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

    @Override
    protected List<DataPointSnapshot> createSnapshots(D datapoint, List<String> dynamicLabelValues) {
        Number value = snapshotGetter.apply(datapoint);
        if (value == null) {
            return List.of();
        }
        return List.of(createSnapshot(value.doubleValue(), dynamicLabelValues));
    }

    public static class Builder<D> extends StatefulMetric.Builder<D, Builder<D>, GaugeAdapter<D>> {

        private Function<D, Number> snapshotGetter;
        private Consumer<D> reset;

        private Builder(String name) {
            super(name);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
        }

        public Builder<D> withSnapshotGetter(Function<D, Number> snapshotGetter) {
            this.snapshotGetter = Objects.requireNonNull(snapshotGetter, "Snapshot getter must not be null");
            return this;
        }

        public Builder<D> withReset(Consumer<D> reset) {
            this.reset = Objects.requireNonNull(reset, "Value reset must not be null");
            return this;
        }

        @Override
        public Builder<D> withContainerFactory(Supplier<D> valueContainerFactory) {
            return super.withContainerFactory(valueContainerFactory);
        }

        @Override
        protected GaugeAdapter<D> buildMetric() {
            return new GaugeAdapter<>(this);
        }

        @Override
        protected Builder<D> self() {
            return this;
        }
    }
}
