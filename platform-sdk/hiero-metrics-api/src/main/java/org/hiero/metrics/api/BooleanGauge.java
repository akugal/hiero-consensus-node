// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static org.hiero.metrics.api.core.MetricUtils.ONE;
import static org.hiero.metrics.api.core.MetricUtils.ZERO;

import java.util.List;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
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

    @Override
    protected List<DataPointSnapshot> createSnapshots(
            BooleanGaugeDataPoint datapoint, List<String> dynamicLabelValues) {
        return List.of(createSnapshot(datapoint.getAsBoolean() ? ONE : ZERO, dynamicLabelValues));
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
