// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.datapoint.LongCounterDataPoint;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultLongCounter extends AbstractStatefulMetric<LongCounterDataPoint> implements LongCounter {

    public DefaultLongCounter(LongCounter.Builder builder) {
        super(builder);
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
}
