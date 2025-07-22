// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.DoubleCounter;
import org.hiero.metrics.api.datapoint.DoubleCounterDataPoint;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultDoubleCounter extends AbstractStatefulMetric<DoubleCounterDataPoint>
        implements DoubleCounter {

    public DefaultDoubleCounter(DoubleCounter.Builder builder) {
        super(builder);
    }

    @Override
    protected void reset(DoubleCounterDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(DoubleCounterDataPoint datapoint) {
        return List.of(new DataPointSnapshot.ValueItem(datapoint.getAsDouble()));
    }

    @Override
    public void increment(double value) {
        getNoLabels().increment(value);
    }

    @Override
    public double getAsDouble() {
        return getNoLabels().getAsDouble();
    }
}
