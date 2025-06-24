package org.hiero.metrics.api;

import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongAdderCounterDataPoint;
import org.hiero.metrics.api.datapoint.impl.AtomicLongCounterDataPoint;

import java.util.List;

public final class LongCounter
        extends StatefulMetric<LongCounterDataPoint>
        implements LongCounterDataPoint {

    private LongCounter(Builder builder) {
        super(builder);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(LongCounterDataPoint datapoint, List<String> dynamicLabelValues) {
        return List.of(createSnapshot(datapoint.getAsLong(), PrimitiveDataType.LONG, dynamicLabelValues));
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
