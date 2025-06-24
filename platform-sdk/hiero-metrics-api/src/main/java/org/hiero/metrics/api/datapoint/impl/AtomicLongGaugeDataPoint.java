package org.hiero.metrics.api.datapoint.impl;

import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public class AtomicLongGaugeDataPoint implements LongGaugeDataPoint {

    protected final AtomicLong container;

    private final LongSupplier initializer;

    public AtomicLongGaugeDataPoint(LongSupplier initializer) {
        this.initializer = initializer;
        container = new AtomicLong(initializer.getAsLong());
    }

    public AtomicLongGaugeDataPoint(long initValue) {
        this(() -> initValue);
    }

    public AtomicLongGaugeDataPoint() {
        this(0L);
    }

    @Override
    public long getInitValue() {
        return initializer.getAsLong();
    }

    @Override
    public void update(long value) {
        container.set(value);
    }

    @Override
    public long getAndReset() {
        return container.getAndSet(getInitValue());
    }

    @Override
    public long getAsLong() {
        return container.get();
    }
}
