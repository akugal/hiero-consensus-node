package org.hiero.metrics.api.datapoint.impl;

import java.util.concurrent.atomic.AtomicLong;

public final class AtomicLongCounterDataPoint extends AbstractLongCounterDataPoint {

    private final AtomicLong atomicLong = new AtomicLong();

    @Override
    protected void safeIncrement(long value) {
        atomicLong.addAndGet(value);
    }

    @Override
    public long getAsLong() {
        return atomicLong.get();
    }
}