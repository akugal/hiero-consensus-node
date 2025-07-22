// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.internal.DefaultLongCounter;
import org.hiero.metrics.internal.datapoint.LongAdderCounterDataPoint;

public interface LongCounter extends StatefulMetric<LongCounterDataPoint>, LongCounterDataPoint {

    static Builder builder(String name) {
        return new Builder(name);
    }

    final class Builder extends StatefulMetric.Builder<LongCounterDataPoint, Builder, LongCounter> {

        private Builder(String name) {
            super(name, LongAdderCounterDataPoint::new);
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
