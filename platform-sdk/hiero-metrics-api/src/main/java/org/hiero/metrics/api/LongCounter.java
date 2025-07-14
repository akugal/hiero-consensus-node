// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.core.snapshot.DataPointSnapshot;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicLongCounterDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongAdderCounterDataPoint;

public final class LongCounter extends StatefulMetric<LongCounterDataPoint> implements LongCounterDataPoint {

    private LongCounter(Builder builder) {
        super(builder);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    protected void reset(LongCounterDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(LongCounterDataPoint datapoint) {
        return List.of(new DataPointSnapshot.ValueItem(datapoint.getAsLong()));
    }

    @Override
    public void increment(long value) {
        getNoLabels().increment(value);
    }

    @Override
    public long getAsLong() {
        return getNoLabels().getAsLong();
    }

    public static class Builder extends StatefulMetric.Builder<LongCounterDataPoint, Builder, LongCounter> {

        private Builder(String name) {
            super(name);
            withContainerFactory(LongAdderCounterDataPoint::new);
        }

        @Override
        protected MetricType getType() {
            return MetricType.COUNTER;
        }

        public Builder withLowContentionUpdates() {
            withContainerFactory(AtomicLongCounterDataPoint::new);
            return this;
        }

        @Override
        public LongCounter buildMetric() {
            return new LongCounter(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
