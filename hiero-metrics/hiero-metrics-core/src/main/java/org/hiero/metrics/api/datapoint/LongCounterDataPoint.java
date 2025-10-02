// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 * A data point representing a counter that holds a {@code long} value.
 * The counter can be incremented by a specified value or by {@code 1L}.
 * <p>
 * This interface extends {@link LongSupplier} to provide the current value of the counter.
 */
public interface LongCounterDataPoint extends LongSupplier, DoubleSupplier, DataPoint {

    /**
     * Increments the counter by the specified value.
     *
     * @param value the value to increment the counter by (should be non-negative)
     * @throws IllegalArgumentException if the value is negative
     */
    void increment(long value) throws IllegalArgumentException;

    /**
     * Increments the counter by <code>1</code>.
     */
    default void increment() {
        increment(1);
    }

    @Override
    default double getAsDouble() {
        return getAsLong();
    }
}
