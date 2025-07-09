// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.LongSupplier;

public interface LongCounterDataPoint extends LongSupplier, DataPoint {

    long getCreatedTimeMillis();

    /**
     * Increments the counter by the specified value.
     *
     * @param value the value to increment the counter by (should be non-negative)
     */
    void increment(long value);

    /**
     * Increments the counter by 1.
     */
    default void increment() {
        increment(1);
    }
}
