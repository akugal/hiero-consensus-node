// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.stat.StatUtils;

/**
 * A gauge data point that holds a {@link double} value that can be updated.
 * The gauge can use additional calculations/aggregations on observed values.
 * <p>
 * This interface extends {@link DoubleSupplier} to provide the current value of the gauge.
 */
public interface DoubleGaugeDataPoint extends DoubleSupplier, DataPoint {

    /**
     * Get the initial value of the gauge.
     *
     * @return the initial value
     */
    double getInitValue();

    /**
     * Update the gauge by {@code 1.0}.
     */
    default void update() {
        update(StatUtils.ONE);
    }

    /**
     * Update the gauge by the specified value.
     * The value can be positive or negative.
     *
     * @param value the value to update the gauge by
     */
    void update(double value);

    /**
     * Reset the gauge to its initial value and return the value before reset.
     *
     * @return the value before reset
     */
    double getAndReset();
}
