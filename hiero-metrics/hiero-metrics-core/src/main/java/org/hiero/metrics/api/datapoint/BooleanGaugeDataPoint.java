// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.BooleanSupplier;

/**
 * A {@link DataPoint} that represents a boolean gauge value.
 * <p>
 * The value can be set to true or false using {@link #setTrue()} and {@link #setFalse()} methods,
 * or to an arbitrary boolean value using {@link #set(boolean)} method.
 * <p>
 * The current value can be retrieved using the {@link #getAsBoolean()} method inherited from
 * {@link BooleanSupplier}.
 */
public interface BooleanGaugeDataPoint extends BooleanSupplier, DataPoint {

    /**
     * Sets the value of this boolean gauge data point.
     *
     * @param value the new boolean value to set
     */
    void set(boolean value);

    /**
     * Sets the value of this boolean gauge data point to {@code true}.
     */
    default void setTrue() {
        set(true);
    }

    /**
     * Sets the value of this boolean gauge data point to {@code false}.
     */
    default void setFalse() {
        set(false);
    }
}
