// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

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
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatUtils;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicLongGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongAccumulatorGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongGaugeCompositeArrayDataPoint;

public final class LongGaugeComposite extends StatefulMetric<LongGaugeCompositeDataPoint> {

    private final String classifierLabel;
    private final String[] dataPointNames;
    private final ToLongFunction<LongGaugeDataPoint> snapshotValueSupplier;

    private LongGaugeComposite(Builder builder) {
        super(builder);

        classifierLabel = Objects.requireNonNull(builder.classifierLabel, "Classifier label must not be null");
        dataPointNames = Objects.requireNonNull(builder.dataPointNames, "Data point names must not be null")
                .toArray(new String[0]);
        snapshotValueSupplier =
                Objects.requireNonNull(builder.snapshotValueSupplier, "Snapshot value supplier must not be null");
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(
            LongGaugeCompositeDataPoint datapoint, List<String> dynamicLabelValues) {
        List<DataPointSnapshot> snapshots = new ArrayList<>(datapoint.size());
        for (int i = 0; i < datapoint.size(); i++) {
            long value = snapshotValueSupplier.applyAsLong(datapoint.get(i));
            if (Long.MAX_VALUE != value && Long.MIN_VALUE != value) {
                snapshots.add(createSnapshot(
                        value,
                        PrimitiveDataType.LONG,
                        dynamicLabelValues,
                        new Label(classifierLabel, dataPointNames[i])));
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

        public Builder withClassifierLabel(String classifierLabel) {
            this.classifierLabel = classifierLabel;
            return this;
        }

        public Builder withAccumulatorDataPoint(String name, LongBinaryOperator operator, long initValue) {
            dataPointNames.add(name);
            dataPointFactories.add(() -> new LongAccumulatorGaugeDataPoint(operator, initValue));
            return this;
        }

        public Builder withSum() {
            return withAccumulatorDataPoint("sum", StatUtils.LONG_SUM, 0L);
        }

        public Builder withMax() {
            return withAccumulatorDataPoint("max", StatUtils.LONG_MAX, Long.MIN_VALUE);
        }

        public Builder withMin() {
            return withAccumulatorDataPoint("min", StatUtils.LONG_MIN, Long.MAX_VALUE);
        }

        public Builder withLatestDataPoint(String name) {
            return withLatestDataPoint(name, 0L);
        }

        public Builder withLatestDataPoint(String name, long initValue) {
            dataPointNames.add(name);
            dataPointFactories.add(() -> new AtomicLongGaugeDataPoint(initValue));
            return this;
        }

        public Builder resetOnSnapshot() {
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
