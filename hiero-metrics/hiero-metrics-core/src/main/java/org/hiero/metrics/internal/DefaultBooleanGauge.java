// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import static org.hiero.metrics.api.stat.StatUtils.ONE;
import static org.hiero.metrics.api.stat.StatUtils.ZERO;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.BooleanGauge;
import org.hiero.metrics.api.datapoint.BooleanGaugeDataPoint;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultBooleanGauge extends AbstractStatefulMetric<BooleanGaugeDataPoint> implements BooleanGauge {

    public DefaultBooleanGauge(BooleanGauge.Builder builder) {
        super(builder);
    }

    @Override
    protected void reset(BooleanGaugeDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(BooleanGaugeDataPoint datapoint) {
        return List.of(new DataPointSnapshot.ValueItem(datapoint.getAsBoolean() ? ONE : ZERO));
    }
}
