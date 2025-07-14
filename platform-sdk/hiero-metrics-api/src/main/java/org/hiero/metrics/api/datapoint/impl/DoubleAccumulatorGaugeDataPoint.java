// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import static org.hiero.metrics.api.core.MetricUtils.ZERO;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.LongBinaryOperator;

public class DoubleAccumulatorGaugeDataPoint extends AtomicDoubleGaugeDataPoint {

    private final LongBinaryOperator operator;

    public DoubleAccumulatorGaugeDataPoint(DoubleBinaryOperator operator, DoubleSupplier initializer) {
        super(initializer);
        this.operator = (prev, cur) -> fromDouble(operator.applyAsDouble(toDouble(prev), toDouble(cur)));
    }

    public DoubleAccumulatorGaugeDataPoint(DoubleBinaryOperator operator, double initialValue) {
        this(operator, initialValue == ZERO ? DEFAULT_INIT : () -> initialValue);
    }

    public DoubleAccumulatorGaugeDataPoint(DoubleBinaryOperator operator) {
        this(operator, DEFAULT_INIT);
    }

    @Override
    public void update(double value) {
        container.accumulateAndGet(fromDouble(value), operator);
    }
}
