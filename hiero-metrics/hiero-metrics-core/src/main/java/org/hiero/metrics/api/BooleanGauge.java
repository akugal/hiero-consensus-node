// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.BooleanGaugeDataPoint;
import org.hiero.metrics.internal.DefaultBooleanGauge;
import org.hiero.metrics.internal.datapoint.AtomicBooleanGaugeDataPoint;

public interface BooleanGauge extends StatefulMetric<BooleanGaugeDataPoint> {

    static MetricKey<BooleanGauge> key(String name) {
        return MetricKey.of(name, BooleanGauge.class);
    }

    static MetricKey<BooleanGauge> key(String category, String name) {
        return MetricKey.of(category, name, BooleanGauge.class);
    }

    static Builder builder(MetricKey<BooleanGauge> key) {
        return new Builder(key);
    }

    final class Builder extends StatefulMetric.Builder<BooleanGaugeDataPoint, Builder, BooleanGauge> {

        private Builder(MetricKey<BooleanGauge> key) {
            super(key, AtomicBooleanGaugeDataPoint::new);
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
