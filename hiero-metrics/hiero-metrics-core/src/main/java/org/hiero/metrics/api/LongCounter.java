// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.internal.DefaultLongCounter;
import org.hiero.metrics.internal.datapoint.LongAdderCounterDataPoint;

public interface LongCounter extends StatefulMetric<LongCounterDataPoint>, LongCounterDataPoint {

    static MetricKey<LongCounter> key(String name) {
        return MetricKey.of(name, LongCounter.class);
    }

    static MetricKey<LongCounter> key(String category, String name) {
        return MetricKey.of(category, name, LongCounter.class);
    }

    static Builder builder(MetricKey<LongCounter> key) {
        return new Builder(key);
    }

    final class Builder extends StatefulMetric.Builder<LongCounterDataPoint, Builder, LongCounter> {

        private Builder(MetricKey<LongCounter> key) {
            super(key, LongAdderCounterDataPoint::new);
        }

        @Override
        public MetricType getType() {
            return MetricType.COUNTER;
        }

        @Override
        public LongCounter buildMetric() {
            return new DefaultLongCounter(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
