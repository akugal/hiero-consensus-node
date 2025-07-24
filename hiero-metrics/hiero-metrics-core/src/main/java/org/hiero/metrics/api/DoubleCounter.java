// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleCounterDataPoint;
import org.hiero.metrics.internal.DefaultDoubleCounter;
import org.hiero.metrics.internal.datapoint.DoubleAdderCounterDataPoint;

public interface DoubleCounter extends StatefulMetric<DoubleCounterDataPoint>, DoubleCounterDataPoint {

    static MetricKey<DoubleCounter> key(String name) {
        return MetricKey.of(name, DoubleCounter.class);
    }

    static MetricKey<DoubleCounter> key(String category, String name) {
        return MetricKey.of(category, name, DoubleCounter.class);
    }

    static Builder builder(MetricKey<DoubleCounter> key) {
        return new Builder(key);
    }

    final class Builder extends StatefulMetric.Builder<DoubleCounterDataPoint, Builder, DoubleCounter> {

        private Builder(MetricKey<DoubleCounter> key) {
            super(key, DoubleAdderCounterDataPoint::new);
        }

        @Override
        public MetricType getType() {
            return MetricType.COUNTER;
        }

        @Override
        public DoubleCounter buildMetric() {
            return new DefaultDoubleCounter(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
