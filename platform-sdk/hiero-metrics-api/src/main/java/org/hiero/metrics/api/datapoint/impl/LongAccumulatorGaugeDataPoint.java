// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.function.LongBinaryOperator;
import java.util.function.LongSupplier;

public final class LongAccumulatorGaugeDataPoint extends AtomicLongGaugeDataPoint {

    private final LongBinaryOperator operator;

    public LongAccumulatorGaugeDataPoint(LongBinaryOperator operator, LongSupplier initializer) {
        super(initializer);
        this.operator = operator;
    }

    public LongAccumulatorGaugeDataPoint(LongBinaryOperator operator, long initialValue) {
        this(operator, initialValue == 0L ? DEFAULT_INIT : () -> initialValue);
    }

    public LongAccumulatorGaugeDataPoint(LongBinaryOperator operator) {
        this(operator, DEFAULT_INIT);
    }

    @Override
    public void update(long value) {
        container.accumulateAndGet(value, operator);
    }
}
