// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.MetricUtils;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.core.snapshot.DataPointSnapshot;
import org.hiero.metrics.api.datapoint.BooleanGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicBooleanGaugeDataPoint;

public final class BooleanGauge extends StatefulMetric<BooleanGaugeDataPoint> implements BooleanGaugeDataPoint {

    private BooleanGauge(Builder builder) {
        super(builder);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    protected void reset(BooleanGaugeDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(BooleanGaugeDataPoint datapoint) {
        return List.of(new DataPointSnapshot.ValueItem(datapoint.getAsBoolean() ? MetricUtils.ONE : MetricUtils.ZERO));
    }

    @Override
    public void update(boolean value) {
        getNoLabels().update(value);
    }

    @Override
    public boolean getAsBoolean() {
        return getNoLabels().getAsBoolean();
    }

    public static class Builder extends StatefulMetric.Builder<BooleanGaugeDataPoint, Builder, BooleanGauge> {

        private Builder(String name) {
            super(name);
            withContainerFactory(AtomicBooleanGaugeDataPoint::new);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
        }

        @Override
        public BooleanGauge buildMetric() {
            return new BooleanGauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
