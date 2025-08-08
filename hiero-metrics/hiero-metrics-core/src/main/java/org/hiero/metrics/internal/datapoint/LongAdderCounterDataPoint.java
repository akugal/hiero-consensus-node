// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;
import org.hiero.metrics.api.stat.StatUtils;

public final class LongAdderCounterDataPoint extends AbstractLongCounterDataPoint {

    private final LongSupplier initializer;
    private final LongAdder container = new LongAdder();

    public LongAdderCounterDataPoint() {
        this(StatUtils.LONG_INIT);
    }

    public LongAdderCounterDataPoint(LongSupplier initializer) {
        this.initializer = initializer;
        reset();
    }

    @Override
    protected void safeIncrement(long value) {
        container.add(value);
    }

    @Override
    public long getAsLong() {
        return container.sum();
    }

    @Override
    public void reset() {
        container.reset();
        increment(initializer.getAsLong());
    }
}
