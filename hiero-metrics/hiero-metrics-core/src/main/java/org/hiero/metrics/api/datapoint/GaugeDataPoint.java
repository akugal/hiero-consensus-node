// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;

/**
 * A gauge data point for a generic type that is converted to {@code double}.
 *
 * @param <T> the type of value used to update the gauge
 */
public interface GaugeDataPoint<T> extends DoubleSupplier, DataPoint {

    /**
     * Update the gauge with a new value.
     *
     * @param value the new value
     */
    void update(T value);
}
