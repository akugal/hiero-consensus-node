// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.function.Supplier;
import org.hiero.metrics.api.GenericGauge;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultGenericGauge<T> extends AbstractStatefulMetric<Supplier<T>, GaugeDataPoint<T>>
        implements GenericGauge<T> {

    public DefaultGenericGauge(GenericGauge.Builder<T> builder) {
        super(builder);
    }

    @Override
    protected void reset(GaugeDataPoint<T> dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> exportDataPoint(GaugeDataPoint<T> datapoint) {
        double value = datapoint.getAsDouble();
        if (Double.isNaN(value)) {
            return List.of();
        }
        return List.of(new DataPointSnapshot.ValueItem(value));
    }
}
