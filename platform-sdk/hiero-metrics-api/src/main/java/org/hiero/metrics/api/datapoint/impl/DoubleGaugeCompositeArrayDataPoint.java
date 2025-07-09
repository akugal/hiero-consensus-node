// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.List;
import java.util.function.Supplier;
import org.hiero.metrics.api.datapoint.DoubleGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;

public class DoubleGaugeCompositeArrayDataPoint implements DoubleGaugeCompositeDataPoint {

    private final DoubleGaugeDataPoint[] dataPoints;

    public DoubleGaugeCompositeArrayDataPoint(List<Supplier<DoubleGaugeDataPoint>> dataPointFactories) {
        if (dataPointFactories == null || dataPointFactories.isEmpty()) {
            throw new IllegalArgumentException("Data points factory list cannot be null or empty");
        }
        this.dataPoints = dataPointFactories.stream().map(Supplier::get).toArray(DoubleGaugeDataPoint[]::new);
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
