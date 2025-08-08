// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.function.LongSupplier;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.internal.DefaultLongCounter;
import org.hiero.metrics.internal.datapoint.AtomicLongCounterDataPoint;
import org.hiero.metrics.internal.datapoint.LongAdderCounterDataPoint;

public interface LongCounter extends StatefulMetric<LongSupplier, LongCounterDataPoint> {

    static MetricKey<LongCounter> key(String name) {
        return MetricKey.of(name, LongCounter.class);
    }

    static MetricKey<LongCounter> key(String category, String name) {
        return MetricKey.of(category, name, LongCounter.class);
    }

    static Builder builder(MetricKey<LongCounter> key) {
        return new Builder(key);
    }

    final class Builder extends StatefulMetric.Builder<LongSupplier, LongCounterDataPoint, Builder, LongCounter> {

        private Builder(MetricKey<LongCounter> key) {
            super(MetricType.COUNTER, key, StatUtils.LONG_INIT, LongAdderCounterDataPoint::new);
        }

        @NonNull
        public Builder withInitValue(long initValue) {
            return withDefaultInitializer(StatUtils.asInitializer(initValue));
        }

        @NonNull
        public Builder withLowThreadContention() {
            withContainerFactory(AtomicLongCounterDataPoint::new);
            return this;
        }

        @NonNull
        @Override
        public LongCounter buildMetric() {
            return new DefaultLongCounter(this);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
