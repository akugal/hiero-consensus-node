// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.DoubleGauge;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultDoubleGauge extends AbstractStatefulMetric<DoubleGaugeDataPoint> implements DoubleGauge {

    private final ToDoubleFunction<DoubleGaugeDataPoint> snapshotValueSupplier;

    public DefaultDoubleGauge(DoubleGauge.Builder builder) {
        super(builder);

        snapshotValueSupplier =
                builder.isResetOnSnapshot() ? DoubleGaugeDataPoint::getAndReset : DoubleGaugeDataPoint::getAsDouble;
    }

    @Override
    protected void reset(DoubleGaugeDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(DoubleGaugeDataPoint datapoint) {
        double value = snapshotValueSupplier.applyAsDouble(datapoint);
        if (Double.MAX_VALUE == value || Double.MIN_VALUE == value) {
            // This is a safeguard against using double extreme values as a valid metric value.
            // MAX_VALUE or MIN_VALUE could be initial values for min or max statistics,
            // but they should not be reported as actual metric values.
            return List.of();
        }
        return List.of(new DataPointSnapshot.ValueItem(datapoint.getAsDouble()));
    }
}
