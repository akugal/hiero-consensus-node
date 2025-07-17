// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleCounterDataPoint;
import org.hiero.metrics.internal.DefaultDoubleCounter;
import org.hiero.metrics.internal.datapoint.DoubleAdderCounterDataPoint;

public interface DoubleCounter extends StatefulMetric<DoubleCounterDataPoint>, DoubleCounterDataPoint {

    static Builder builder(String name) {
        return new Builder(name);
    }

    final class Builder extends StatefulMetric.Builder<DoubleCounterDataPoint, Builder, DoubleCounter> {

        private Builder(String name) {
            super(name, DoubleAdderCounterDataPoint::new);
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
