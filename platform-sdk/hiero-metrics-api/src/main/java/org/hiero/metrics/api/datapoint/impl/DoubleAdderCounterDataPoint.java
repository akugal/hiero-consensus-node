// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.concurrent.atomic.DoubleAdder;
import org.hiero.metrics.api.datapoint.DoubleCounterDataPoint;

public final class DoubleAdderCounterDataPoint implements DoubleCounterDataPoint {

    private final long createdTimeMillis = System.currentTimeMillis();
    private final DoubleAdder adder = new DoubleAdder();

    @Override
    public long getCreatedTimeMillis() {
        return createdTimeMillis;
    }

    @Override
    public void increment(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Increment value must be non-negative, but was: " + value);
        }
        adder.add(value);
    }

    @Override
    public void increment() {
        adder.add(1);
    }

    @Override
    public double getAsDouble() {
        return adder.sum();
    }
}
