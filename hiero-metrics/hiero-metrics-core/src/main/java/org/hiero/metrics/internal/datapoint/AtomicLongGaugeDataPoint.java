// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;

public class AtomicLongGaugeDataPoint implements LongGaugeDataPoint {

    protected final AtomicLong container;

    private final LongSupplier initializer;

    public AtomicLongGaugeDataPoint(LongSupplier initializer) {
        this.initializer = initializer;
        container = new AtomicLong(initializer.getAsLong());
    }

    public AtomicLongGaugeDataPoint(long initValue) {
        this(initValue == 0L ? DEFAULT_INIT : () -> initValue);
    }

    public AtomicLongGaugeDataPoint() {
        this(DEFAULT_INIT);
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

    @Override
    public void reset() {
        container.set(getInitValue());
    }
}
