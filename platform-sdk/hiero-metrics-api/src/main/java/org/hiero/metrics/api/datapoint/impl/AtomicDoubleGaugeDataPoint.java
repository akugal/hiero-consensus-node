package org.hiero.metrics.api.datapoint.impl;

import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.LongBinaryOperator;

public class AtomicDoubleGaugeDataPoint implements DoubleGaugeDataPoint {

    private final DoubleSupplier initializer;
    protected final AtomicLong container;

    public AtomicDoubleGaugeDataPoint(DoubleSupplier initializer) {
        this.initializer = initializer;
        container = new AtomicLong(fromDouble(initializer.getAsDouble()));
    }

    public AtomicDoubleGaugeDataPoint(double initialValue) {
        this(() -> initialValue);
    }

    public AtomicDoubleGaugeDataPoint() {
        this(0.0);
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
}
