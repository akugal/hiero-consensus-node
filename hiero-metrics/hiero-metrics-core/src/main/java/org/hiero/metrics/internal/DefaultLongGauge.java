// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.function.ToLongFunction;
import org.hiero.metrics.api.LongGauge;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultLongGauge extends AbstractStatefulMetric<LongGaugeDataPoint> implements LongGauge {

    private final ToLongFunction<LongGaugeDataPoint> snapshotValueSupplier;

    public DefaultLongGauge(LongGauge.Builder builder) {
        super(builder);

        snapshotValueSupplier =
                builder.isResetOnSnapshot() ? LongGaugeDataPoint::getAndReset : LongGaugeDataPoint::getAsLong;
    }

    @Override
    protected void reset(LongGaugeDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(LongGaugeDataPoint datapoint) {
        long value = snapshotValueSupplier.applyAsLong(datapoint);
        if (Long.MAX_VALUE == value || Long.MIN_VALUE == value) {
            // This is a safeguard against using long extreme values as a valid metric value.
            // MAX_VALUE or MIN_VALUE could be initial values for min or max statistics,
            // but they should not be reported as actual metric values.
            return List.of();
        }
        return List.of(new DataPointSnapshot.ValueItem(value));
    }
}
