// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.Objects;
import java.util.function.Supplier;
import org.hiero.metrics.api.datapoint.DoubleGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;

public class DoubleGaugeCompositeArrayDataPoint implements DoubleGaugeCompositeDataPoint {

    private final DoubleGaugeDataPoint[] dataPoints;

    public DoubleGaugeCompositeArrayDataPoint(Supplier<DoubleGaugeDataPoint[]> dataPointFactories) {
        Objects.requireNonNull(dataPointFactories, "Data point factories must not be null");
        this.dataPoints = Objects.requireNonNull(dataPointFactories.get(), "Data point factories must not be null");
    }

    @Override
    public void update(double value) {
        for (DoubleGaugeDataPoint dataPoint : dataPoints) {
            dataPoint.update(value);
        }
    }

    @Override
    public int size() {
        return dataPoints.length;
    }

    @Override
    public DoubleGaugeDataPoint get(int index) {
        return dataPoints[index];
    }
}
