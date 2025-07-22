// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.utils.MetricUtils.ZERO;
import static org.hiero.metrics.api.utils.StatUtils.DEFAULT_STAT_LABEL;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Supplier;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.utils.StatUtils;
import org.hiero.metrics.internal.DefaultDoubleGaugeComposite;
import org.hiero.metrics.internal.datapoint.AtomicDoubleGaugeDataPoint;
import org.hiero.metrics.internal.datapoint.DoubleAccumulatorGaugeDataPoint;
import org.hiero.metrics.internal.datapoint.DoubleGaugeCompositeArrayDataPoint;

public interface DoubleGaugeComposite
        extends StatefulMetric<DoubleGaugeCompositeDataPoint>, DoubleGaugeCompositeDataPoint {

    @Override
    void reset();

    static Builder builder(String name) {
        return new Builder(name);
    }

    final class Builder extends StatefulMetric.Builder<DoubleGaugeCompositeDataPoint, Builder, DoubleGaugeComposite> {

        private String statLabel = DEFAULT_STAT_LABEL;
        private final List<String> statNames = new ArrayList<>();
        private final List<Supplier<DoubleGaugeDataPoint>> dataPointFactories = new ArrayList<>();
        private boolean resetOnSnapshot = false;

        private Builder(String name) {
            super(name, () -> new DoubleGaugeCompositeArrayDataPoint(() -> new DoubleGaugeDataPoint[0]));
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        public boolean isResetOnSnapshot() {
            return resetOnSnapshot;
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
        public List<Supplier<DoubleGaugeDataPoint>> getDataPointFactories() {
            return dataPointFactories;
        }

        public Builder withStatLabel(String statLabel) {
            this.statLabel = ArgumentUtils.throwArgBlank(statLabel, "stat label");
            return this;
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
            this.resetOnSnapshot = true;
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

            if (constantLabels.containsKey(statLabel)) {
                throw new IllegalStateException("Stat label '" + statLabel + "' conflicts with a constant label");
            }
            for (String dynamicLabelName : getDynamicLabelNames()) {
                if (dynamicLabelName.equals(statLabel)) {
                    throw new IllegalStateException("Stat label '" + statLabel + "' conflicts with a dynamic label");
                }
            }

            // copy the data point factories to ensure immutability
            final List<Supplier<DoubleGaugeDataPoint>> suppliers = List.copyOf(dataPointFactories);
            Supplier<DoubleGaugeDataPoint[]> dataPonitsSupplier =
                    () -> suppliers.stream().map(Supplier::get).toArray(DoubleGaugeDataPoint[]::new);

            withContainerFactory(() -> new DoubleGaugeCompositeArrayDataPoint(dataPonitsSupplier));
            return new DefaultDoubleGaugeComposite(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
