// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.concurrent.atomic.LongAdder;

public final class LongAdderCounterDataPoint extends AbstractLongCounterDataPoint {

    private final LongAdder adder = new LongAdder();

    @Override
    protected void safeIncrement(long value) {
        adder.add(value);
    }

    @Override
    public long getAsLong() {
        return adder.sum();
    }
}
