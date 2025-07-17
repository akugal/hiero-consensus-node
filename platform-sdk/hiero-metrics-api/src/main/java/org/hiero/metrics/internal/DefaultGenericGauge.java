// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.GenericGauge;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultGenericGauge<T> extends AbstractStatefulMetric<GaugeDataPoint<T>> implements GenericGauge<T> {

    public DefaultGenericGauge(GenericGauge.Builder<T> builder) {
        super(builder);
    }

    @Override
    protected void reset(GaugeDataPoint<T> dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(GaugeDataPoint<T> datapoint) {
        double value = datapoint.getAsDouble();
        if (Double.isNaN(value)) {
            return List.of();
        }
        return List.of(new DataPointSnapshot.ValueItem(value));
    }

    @Override
    public void update(T value) {
        getNoLabels().update(value);
    }

    @Override
    public double getAsDouble() {
        return getNoLabels().getAsDouble();
    }
}
