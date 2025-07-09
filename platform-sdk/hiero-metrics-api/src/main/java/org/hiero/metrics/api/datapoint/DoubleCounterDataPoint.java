// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import static org.hiero.metrics.api.core.MetricUtils.ONE;

import java.util.function.DoubleSupplier;

public interface DoubleCounterDataPoint extends DoubleSupplier, DataPoint {

    long getCreatedTimeMillis();

    /**
     * Increments the counter by the specified value.
     *
     * @param value the value to increment the counter by (should be non-negative)
     */
    void increment(double value);

    /**
     * Increments the counter by 1.
     */
    default void increment() {
        increment(ONE);
    }
}
