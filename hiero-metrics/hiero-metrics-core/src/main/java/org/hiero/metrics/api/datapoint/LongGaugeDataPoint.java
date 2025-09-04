// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.LongSupplier;

/**
 * A gauge data point that holds a <code>long</code> value.
 * The gauge can be incremented, decremented, or updated by a specified value.
 * The gauge can use additional calculations/aggregations on observed values.
 * <p>
 * This interface extends {@link LongSupplier} to provide the current value of the gauge.
 */
public interface LongGaugeDataPoint extends LongSupplier, DataPoint {

    /**
     * Gets the initial value of the gauge when it was created.
     *
     * @return the initial value of the gauge
     */
    long getInitValue();

    /**
     * Increments the gauge by {@code 1L}.
     */
    default void increment() {
        update(1L);
    }

    /**
     * Decrements the gauge by {@code 1L}.
     */
    default void decrement() {
        update(-1L);
    }

    /**
     * Updates the gauge by adding the specified value.
     * The value can be positive or negative.
     *
     * @param value the value to update the gauge
     */
    void update(long value);

    /**
     * Resets the gauge to its initial value and returns the value before reset.
     *
     * @return the value before reset
     */
    long getAndReset();

    /**
     * {@inheritDoc}
     */
    @Override
    default void reset() {
        getAndReset();
    }
}
