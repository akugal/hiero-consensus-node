// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import org.hiero.metrics.api.stat.StatUtils;

public final class AtomicLongCounterDataPoint extends AbstractLongCounterDataPoint {

    private final LongSupplier initializer;
    private final AtomicLong container = new AtomicLong();

    public AtomicLongCounterDataPoint() {
        this(StatUtils.LONG_INIT);
    }

    public AtomicLongCounterDataPoint(LongSupplier initializer) {
        this.initializer = initializer;
        reset();
    }

    @Override
    protected void safeIncrement(long value) {
        container.addAndGet(value);
    }

    @Override
    public long getAsLong() {
        return container.get();
    }

    @Override
    public void reset() {
        container.set(initializer.getAsLong());
    }
}
