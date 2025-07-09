// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import static org.hiero.metrics.api.core.MetricUtils.ZERO;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;

public class AtomicDoubleGaugeDataPoint implements DoubleGaugeDataPoint {

    private final DoubleSupplier initializer;
    protected final AtomicLong container;

    public AtomicDoubleGaugeDataPoint(DoubleSupplier initializer) {
        this.initializer = initializer;
        container = new AtomicLong(fromDouble(initializer.getAsDouble()));
    }

    public AtomicDoubleGaugeDataPoint(double initialValue) {
        this(initialValue == ZERO ? DEFAULT_INIT : () -> initialValue);
    }

    public AtomicDoubleGaugeDataPoint() {
        this(DEFAULT_INIT);
    }

    @Override
    public double getInitValue() {
        return initializer.getAsDouble();
    }

    @Override
    public void update(double value) {
        container.set(fromDouble(value));
    }

    @Override
    public double getAndReset() {
        return toDouble(container.getAndSet(fromDouble(getInitValue())));
    }

    @Override
    public double getAsDouble() {
        return toDouble(container.get());
    }

    protected long fromDouble(double value) {
        return Double.doubleToRawLongBits(value);
    }

    protected double toDouble(long value) {
        return Double.longBitsToDouble(value);
    }

    @Override
    public void reset() {
        container.set(fromDouble(getInitValue()));
    }
}
