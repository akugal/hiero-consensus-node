// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.DoubleCounterDataPoint;
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.internal.DefaultDoubleCounter;
import org.hiero.metrics.internal.datapoint.DoubleAdderCounterDataPoint;

public interface DoubleCounter extends StatefulMetric<DoubleSupplier, DoubleCounterDataPoint> {

    static MetricKey<DoubleCounter> key(String name) {
        return MetricKey.of(name, DoubleCounter.class);
    }

    static Builder builder(MetricKey<DoubleCounter> key) {
        return new Builder(key);
    }

    final class Builder extends StatefulMetric.Builder<DoubleSupplier, DoubleCounterDataPoint, Builder, DoubleCounter> {

        private Builder(MetricKey<DoubleCounter> key) {
            super(MetricType.COUNTER, key, StatUtils.DOUBLE_INIT, DoubleAdderCounterDataPoint::new);
        }

        @NonNull
        public Builder withInitValue(double initValue) {
            return withDefaultInitializer(StatUtils.asInitializer(initValue));
        }

        @NonNull
        @Override
        public DoubleCounter buildMetric() {
            return new DefaultDoubleCounter(this);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
