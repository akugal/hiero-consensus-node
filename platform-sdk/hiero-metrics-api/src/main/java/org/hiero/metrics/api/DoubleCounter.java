// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.core.snapshot.DataPointSnapshot;
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
    protected void reset(DoubleCounterDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(DoubleCounterDataPoint datapoint) {
        return List.of(new DataPointSnapshot.ValueItem(datapoint.getAsDouble()));
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
        protected MetricType getType() {
            return MetricType.COUNTER;
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
