// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.IntegerPairGauge;
import org.hiero.metrics.api.datapoint.IntegerPairDataPoint;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public class DefaultIntegerPairGauge extends AbstractStatefulMetric<IntegerPairDataPoint> implements IntegerPairGauge {

    private final ToDoubleFunction<IntegerPairDataPoint> snapshotValueSupplier;

    public DefaultIntegerPairGauge(IntegerPairGauge.Builder builder) {
        super(builder);

        snapshotValueSupplier =
                builder.isResetOnSnapshot() ? IntegerPairDataPoint::getAndReset : IntegerPairDataPoint::getAsDouble;
    }

    @Override
    public void update(int left, int right) {
        getNoLabels().update(left, right);
    }

    @Override
    public int getLeft() {
        return getNoLabels().getLeft();
    }

    @Override
    public int getRight() {
        return getNoLabels().getRight();
    }

    @Override
    public double getAndReset() {
        return getNoLabels().getAndReset();
    }

    @Override
    public double getAsDouble() {
        return getNoLabels().getAsDouble();
    }

    @Override
    protected void reset(IntegerPairDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(IntegerPairDataPoint datapoint) {
        return List.of(new DataPointSnapshot.ValueItem(snapshotValueSupplier.applyAsDouble(datapoint)));
    }
}
