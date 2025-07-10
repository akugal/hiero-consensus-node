// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import com.swirlds.base.ArgumentUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatUtils;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicDoubleGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.DoubleAccumulatorGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.DoubleGaugeCompositeArrayDataPoint;

import static org.hiero.metrics.api.core.MetricUtils.ZERO;

public final class DoubleGaugeComposite extends StatefulMetric<DoubleGaugeCompositeDataPoint>
        implements DoubleGaugeCompositeDataPoint {

    private final String[] statNames;
    private final ToDoubleFunction<DoubleGaugeDataPoint> snapshotValueSupplier;

    private DoubleGaugeComposite(Builder builder) {
        super(builder);

        Objects.requireNonNull(builder.statNames, "Data point names must not be null");

        statNames = builder.statNames.toArray(new String[0]);
        snapshotValueSupplier = builder.snapshotValueSupplier;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public void reset(DoubleGaugeCompositeDataPoint dataPoint) {
        dataPoint.reset();
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(
            DoubleGaugeCompositeDataPoint datapoint, List<String> dynamicLabelValues) {
        List<DataPointSnapshot> snapshots = new ArrayList<>(datapoint.size());
        for (int i = 0; i < datapoint.size(); i++) {
            double value = snapshotValueSupplier.applyAsDouble(datapoint.get(i));
            if (Long.MAX_VALUE != value && Long.MIN_VALUE != value) {
                snapshots.add(createSnapshot(statNames[i], value, dynamicLabelValues));
            }
        }
        return snapshots;
    }

    @Override
    public void update(double value) {
        getNoLabels().update(value);
    }

    @Override
    public int size() {
        return getNoLabels().size();
    }

    @Override
    public DoubleGaugeDataPoint get(int index) {
        return getNoLabels().get(index);
    }

    public static class Builder
            extends StatefulMetric.Builder<DoubleGaugeCompositeDataPoint, Builder, DoubleGaugeComposite> {

        private final List<String> statNames = new ArrayList<>();
        private final List<Supplier<DoubleGaugeDataPoint>> dataPointFactories = new ArrayList<>();
        private ToDoubleFunction<DoubleGaugeDataPoint> snapshotValueSupplier = DoubleGaugeDataPoint::getAsDouble;

        private Builder(String name) {
            super(name);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
        }

        public Builder withAccumulatorStat(String name, DoubleBinaryOperator operator) {
            return withAccumulatorStat(name, operator, ZERO);
        }

        public Builder withAccumulatorStat(String name, DoubleBinaryOperator operator, double initValue) {
            Objects.requireNonNull(operator, "operator must not be null");
            return withStatContainerFactory(name, () -> new DoubleAccumulatorGaugeDataPoint(operator, initValue));
        }

        public Builder withSumStat() {
            return withAccumulatorStat("sum", StatUtils.DOUBLE_SUM, ZERO);
        }

        public Builder withMaxStat() {
            return withAccumulatorStat("max", StatUtils.DOUBLE_MAX, Double.MIN_VALUE);
        }

        public Builder withMinStat() {
            return withAccumulatorStat("min", StatUtils.DOUBLE_MIN, Double.MAX_VALUE);
        }

        public Builder withLatestValueStat() {
            return withLatestValueStat(ZERO);
        }

        public Builder withLatestValueStat(double initValue) {
            return withStatContainerFactory("latest", () -> new AtomicDoubleGaugeDataPoint(initValue));
        }

        private Builder withStatContainerFactory(String statName, Supplier<DoubleGaugeDataPoint> statContainerFactory) {
            ArgumentUtils.throwArgBlank(statName, "stat name");

            statNames.add(statName);
            dataPointFactories.add(statContainerFactory);
            return this;
        }

        public Builder withResetOnSnapshot() {
            snapshotValueSupplier = DoubleGaugeDataPoint::getAndReset;
            return this;
        }

        @Override
        public DoubleGaugeComposite buildMetric() {
            if (dataPointFactories.isEmpty()) {
                throw new IllegalStateException("At least one stat must be defined");
            }

            if (new HashSet<>(statNames).size() != dataPointFactories.size()) {
                throw new IllegalArgumentException("Stat names must be unique");
            }

            withContainerFactory(() -> new DoubleGaugeCompositeArrayDataPoint(dataPointFactories));
            return new DoubleGaugeComposite(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
