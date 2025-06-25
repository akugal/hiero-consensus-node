// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.List;
import java.util.function.Supplier;
import org.hiero.metrics.api.datapoint.LongGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;

public class LongGaugeCompositeArrayDataPoint implements LongGaugeCompositeDataPoint {

    private final LongGaugeDataPoint[] dataPoints;

    public LongGaugeCompositeArrayDataPoint(List<Supplier<LongGaugeDataPoint>> dataPointFactories) {
        if (dataPointFactories == null || dataPointFactories.isEmpty()) {
            throw new IllegalArgumentException("Data points factory list cannot be null or empty");
        }
        this.dataPoints = dataPointFactories.stream().map(Supplier::get).toArray(LongGaugeDataPoint[]::new);
    }

    @Override
    public void update(long value) {
        for (LongGaugeDataPoint dataPoint : dataPoints) {
            dataPoint.update(value);
        }
    }

    @Override
    public int size() {
        return dataPoints.length;
    }

    @Override
    public LongGaugeDataPoint get(int index) {
        return dataPoints[index];
    }
}
