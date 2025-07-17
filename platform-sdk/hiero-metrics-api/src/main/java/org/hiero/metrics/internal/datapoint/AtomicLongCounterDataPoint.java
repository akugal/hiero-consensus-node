// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.concurrent.atomic.AtomicLong;

public final class AtomicLongCounterDataPoint extends AbstractLongCounterDataPoint {

    private final AtomicLong container = new AtomicLong();

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
        container.set(0);
    }
}
