// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.stat.StatUtils;

/**
 * A {@link DataPoint} that represents a counter of {@code double} value that can only be incremented.
 * <p>
 * This interface extends {@link DoubleSupplier} to provide the current value of the counter.
 */
public interface DoubleCounterDataPoint extends DoubleSupplier, DataPoint {

    /**
     * Increments the counter by the specified value.
     *
     * @param value the value to increment the counter by (should be non-negative)
     * @throws IllegalArgumentException if the value is negative
     */
    void increment(double value) throws IllegalArgumentException;

    /**
     * Increments the counter by {@code 1}.
     */
    default void increment() {
        increment(StatUtils.ONE);
    }
}
