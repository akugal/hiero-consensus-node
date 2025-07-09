// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.concurrent.atomic.LongAdder;

public final class LongAdderCounterDataPoint extends AbstractLongCounterDataPoint {

    private final LongAdder container = new LongAdder();

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
    }
}
