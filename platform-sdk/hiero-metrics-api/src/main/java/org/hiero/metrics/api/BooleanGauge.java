// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.BooleanGaugeDataPoint;
import org.hiero.metrics.internal.DefaultBooleanGauge;
import org.hiero.metrics.internal.datapoint.AtomicBooleanGaugeDataPoint;

public interface BooleanGauge extends StatefulMetric<BooleanGaugeDataPoint>, BooleanGaugeDataPoint {

    static Builder builder(String name) {
        return new Builder(name);
    }

    final class Builder extends StatefulMetric.Builder<BooleanGaugeDataPoint, Builder, BooleanGauge> {

        private Builder(String name) {
            super(name, AtomicBooleanGaugeDataPoint::new);
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        @Override
        public BooleanGauge buildMetric() {
            return new DefaultBooleanGauge(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
