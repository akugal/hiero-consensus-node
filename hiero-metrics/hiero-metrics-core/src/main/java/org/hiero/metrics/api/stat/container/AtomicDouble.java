// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.stat.container;

import static org.hiero.metrics.api.stat.StatUtils.DOUBLE_INIT;
import static org.hiero.metrics.api.stat.StatUtils.DOUBLE_SUM;
import static org.hiero.metrics.api.stat.StatUtils.ZERO;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.LongBinaryOperator;

public final class AtomicDouble implements DoubleSupplier {

    private static final LongBinaryOperator SUM_OPERATOR = convertBinaryOperator(DOUBLE_SUM);

    private final DoubleSupplier initializer;
    private final AtomicLong container;

    public AtomicDouble(@NonNull DoubleSupplier initializer) {
        this.initializer = Objects.requireNonNull(initializer, "Initializer cannot be null");
        container = new AtomicLong(fromDouble(initializer.getAsDouble()));
    }

    public AtomicDouble(double initialValue) {
        this(initialValue == ZERO ? DOUBLE_INIT : () -> initialValue);
    }

    public AtomicDouble() {
        this(DOUBLE_INIT);
    }

    public double getInitValue() {
        return initializer.getAsDouble();
    }

    @Override
    public double getAsDouble() {
        return toDouble(container.get());
    }

    public void reset() {
        container.set(fromDouble(getInitValue()));
    }

    public void set(double value) {
        container.set(fromDouble(value));
    }

    public double getAndSet(double newValue) {
        return toDouble(container.getAndSet(fromDouble(newValue)));
    }

    public double getAndReset() {
        return toDouble(container.getAndSet(fromDouble(getInitValue())));
    }

    public boolean compareAndSet(double expectedValue, double newValue) {
        return container.compareAndSet(fromDouble(expectedValue), fromDouble(newValue));
    }

    public double accumulateAndGet(double value, LongBinaryOperator operator) {
        return container.accumulateAndGet(fromDouble(value), operator);
    }

    public double addAndGet(final double delta) {
        return accumulateAndGet(delta, SUM_OPERATOR);
    }

    public static LongBinaryOperator convertBinaryOperator(@NonNull DoubleBinaryOperator operator) {
        Objects.requireNonNull(operator, "Operator cannot be null");
        return (prev, cur) -> fromDouble(operator.applyAsDouble(toDouble(prev), toDouble(cur)));
    }

    private static long fromDouble(double value) {
        return Double.doubleToRawLongBits(value);
    }

    private static double toDouble(long value) {
        return Double.longBitsToDouble(value);
    }
}
