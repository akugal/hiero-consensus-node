// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.function.LongBinaryOperator;
import java.util.function.LongSupplier;

public final class LongAccumulatorGaugeDataPoint extends AtomicLongGaugeDataPoint {

    private final LongBinaryOperator operator;

    public LongAccumulatorGaugeDataPoint(LongBinaryOperator operator, LongSupplier initializer) {
        super(initializer);
        this.operator = operator;
    }

    @Override
    public void update(long value) {
        container.accumulateAndGet(value, operator);
    }
}
