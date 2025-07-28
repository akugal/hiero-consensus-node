// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ToDoubleBiFunction;
import org.hiero.metrics.api.datapoint.IntegerPairDataPoint;

public class AtomicIntegerPairDataPoint implements IntegerPairDataPoint {

    private final AtomicLong container = new AtomicLong();
    private final LongBinaryOperator accumulator;
    private final ToDoubleBiFunction<Integer, Integer> resulFunction;

    public AtomicIntegerPairDataPoint(
            @NonNull final IntBinaryOperator leftAccumulator,
            @NonNull final IntBinaryOperator rightAccumulator,
            @NonNull ToDoubleBiFunction<Integer, Integer> resulFunction) {
        Objects.requireNonNull(leftAccumulator, "left accumulator cannot be null");
        Objects.requireNonNull(rightAccumulator, "right accumulator cannot be null");

        this.resulFunction = Objects.requireNonNull(resulFunction, "result function cannot be null");
        ;
        this.accumulator = (current, supplied) -> {
            final int left = leftAccumulator.applyAsInt(extractLeftInt(current), extractLeftInt(supplied));
            final int right = rightAccumulator.applyAsInt(extractRightInt(current), extractRightInt(supplied));
            return combineInts(left, right);
        };
        ;
    }

    @Override
    public void update(int left, int right) {
        container.accumulateAndGet(combineInts(left, right), accumulator);
    }

    @Override
    public int getLeft() {
        return extractLeftInt(container.get());
    }

    @Override
    public int getRight() {
        return extractRightInt(container.get());
    }

    @Override
    public double getAsDouble() {
        final long pair = container.get();
        return resulFunction.applyAsDouble(extractLeftInt(pair), extractRightInt(pair));
    }

    @Override
    public double getAndReset() {
        final long pair = container.getAndSet(0L);
        return resulFunction.applyAsDouble(extractLeftInt(pair), extractRightInt(pair));
    }

    @Override
    public void reset() {
        container.set(0L);
    }

    /**
     * Extract the left integer from a long
     *
     * @param pair the long to extract from
     * @return the left integer
     */
    private static int extractLeftInt(final long pair) {
        return (int) (pair >> 32);
    }

    /**
     * Extract the right integer from a long
     *
     * @param pair the long to extract from
     * @return the right integer
     */
    private static int extractRightInt(final long pair) {
        return (int) pair;
    }

    /**
     * Combine the two integers into a single long
     *
     * @param left  the left integer
     * @param right the right integer
     * @return the combined long
     */
    private static long combineInts(final int left, final int right) {
        return (((long) left) << 32) | (right & 0xffffffffL);
    }
}
