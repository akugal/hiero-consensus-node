// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import com.swirlds.base.ArgumentUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.LongBinaryOperator;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatUtils;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicLongGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongAccumulatorGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongGaugeCompositeArrayDataPoint;

public final class LongGaugeComposite extends StatefulMetric<LongGaugeCompositeDataPoint> {

    private final Label[] dataPointsLabels;
    private final ToLongFunction<LongGaugeDataPoint> snapshotValueSupplier;

    private LongGaugeComposite(Builder builder) {
        super(builder);

        Objects.requireNonNull(builder.classifierLabel, "Classifier label must not be null");
        Objects.requireNonNull(builder.dataPointNames, "Data point names must not be null");

        dataPointsLabels = builder.dataPointNames.stream()
                .map(name -> new Label(builder.classifierLabel, name))
                .toArray(Label[]::new);
        snapshotValueSupplier = builder.snapshotValueSupplier;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    protected void reset(LongGaugeCompositeDataPoint dataPoint) {
        // TODO
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(
            LongGaugeCompositeDataPoint datapoint, List<String> dynamicLabelValues) {
        List<DataPointSnapshot> snapshots = new ArrayList<>(datapoint.size());
        for (int i = 0; i < datapoint.size(); i++) {
            long value = snapshotValueSupplier.applyAsLong(datapoint.get(i));
            if (Long.MAX_VALUE != value && Long.MIN_VALUE != value) {
                snapshots.add(createSnapshot(value, dynamicLabelValues, dataPointsLabels[i]));
            }
        }
        return snapshots;
    }

    public void update(long value) {
        getNoLabels().update(value);
    }

    public static class Builder
            extends StatefulMetric.Builder<LongGaugeCompositeDataPoint, Builder, LongGaugeComposite> {

        private String classifierLabel;
        private final List<String> dataPointNames = new ArrayList<>();
        private final List<Supplier<LongGaugeDataPoint>> dataPointFactories = new ArrayList<>();
        private ToLongFunction<LongGaugeDataPoint> snapshotValueSupplier = LongSupplier::getAsLong;

        private Builder(String name) {
            super(name);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
        }

        public Builder withClassifierLabel(String classifierLabel) {
            this.classifierLabel = ArgumentUtils.throwArgBlank(classifierLabel, "classifierLabel");
            return this;
        }

        public Builder withAccumulatorDataPoint(String name, LongBinaryOperator operator, long initValue) {
            Objects.requireNonNull(operator, "operator must not be null");
            return withDataPointContainerFactory(name, () -> new LongAccumulatorGaugeDataPoint(operator, initValue));
        }

        public Builder withSumDataPoint() {
            return withAccumulatorDataPoint("sum", StatUtils.LONG_SUM, 0L);
        }

        public Builder withMaxDataPoint() {
            return withAccumulatorDataPoint("max", StatUtils.LONG_MAX, Long.MIN_VALUE);
        }

        public Builder withMinDataPoint() {
            return withAccumulatorDataPoint("min", StatUtils.LONG_MIN, Long.MAX_VALUE);
        }

        public Builder withLatestValueDataPoint(String name) {
            return withLatestValueDataPoint(name, 0L);
        }

        public Builder withLatestValueDataPoint(String name, long initValue) {
            return withDataPointContainerFactory(name, () -> new AtomicLongGaugeDataPoint(initValue));
        }

        private Builder withDataPointContainerFactory(String name, Supplier<LongGaugeDataPoint> dataPointFactory) {
            ArgumentUtils.throwArgBlank(name, "dataPointName");

            dataPointNames.add(name);
            dataPointFactories.add(dataPointFactory);
            return this;
        }

        public Builder withResetOnSnapshot() {
            snapshotValueSupplier = LongGaugeDataPoint::getAndReset;
            return this;
        }

        @Override
        public LongGaugeComposite buildMetric() {
            Objects.requireNonNull(dataPointFactories, "Data point factories must not be null");

            if (new HashSet<>(dataPointNames).size() != dataPointFactories.size()) {
                throw new IllegalStateException("Data point names must be unique");
            }

            withContainerFactory(() -> new LongGaugeCompositeArrayDataPoint(dataPointFactories));
            return new LongGaugeComposite(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
