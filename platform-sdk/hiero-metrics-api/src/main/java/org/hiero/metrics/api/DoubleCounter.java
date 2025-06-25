// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.List;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleCounterDataPoint;
import org.hiero.metrics.api.datapoint.impl.DoubleAdderCounterDataPoint;

public final class DoubleCounter extends StatefulMetric<DoubleCounterDataPoint> implements DoubleCounterDataPoint {

    private DoubleCounter(Builder builder) {
        super(builder);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(
            DoubleCounterDataPoint datapoint, List<String> dynamicLabelValues) {
        return List.of(createSnapshot(
                datapoint.getAsDouble(),
                datapoint.getCreatedTimeMillis(),
                PrimitiveDataType.DOUBLE,
                dynamicLabelValues));
    }

    @Override
    public long getCreatedTimeMillis() {
        return getNoLabels().getCreatedTimeMillis();
    }

    @Override
    public void increment(double value) {
        getNoLabels().increment(value);
    }

    @Override
    public double getAsDouble() {
        return getNoLabels().getAsDouble();
    }

    public static class Builder extends StatefulMetric.Builder<DoubleCounterDataPoint, Builder, DoubleCounter> {

        private Builder(String name) {
            super(name);
            withContainerFactory(DoubleAdderCounterDataPoint::new);
        }

        @Override
        public DoubleCounter buildMetric() {
            return new DoubleCounter(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
