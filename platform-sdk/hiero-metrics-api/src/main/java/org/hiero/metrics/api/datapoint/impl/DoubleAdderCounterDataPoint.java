// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.concurrent.atomic.DoubleAdder;
import org.hiero.metrics.api.datapoint.DoubleCounterDataPoint;

import static org.hiero.metrics.api.core.MetricUtils.ZERO;

public final class DoubleAdderCounterDataPoint implements DoubleCounterDataPoint {

    private final long createdTimeMillis = System.currentTimeMillis();
    private final DoubleAdder container = new DoubleAdder();

    @Override
    public void increment(double value) {
        if (value < ZERO) {
            throw new IllegalArgumentException("Increment value must be non-negative, but was: " + value);
        }
        container.add(value);
    }

    @Override
    public void increment() {
        container.add(1);
    }

    @Override
    public double getAsDouble() {
        return container.sum();
    }

    @Override
    public void reset() {
        container.reset();
    }
}
