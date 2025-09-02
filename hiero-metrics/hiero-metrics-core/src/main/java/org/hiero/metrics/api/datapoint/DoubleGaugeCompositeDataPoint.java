// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

/**
 * A composite data point that contains multiple {@link DoubleGaugeDataPoint} instances.
 * This interface allows updating all contained gauges with a single update and provides access to individual gauges.
 * It is useful for cases when different aggregations (e.g. sum, min, max, avg) are maintained
 * for the same metric with single update call.
 */
public interface DoubleGaugeCompositeDataPoint extends DataPoint {

    /**
     * Updates all contained {@link DoubleGaugeDataPoint} instances with the given value.
     *
     * @param value the value to set for all gauges
     */
    void update(double value);

    /**
     * Returns the number of contained {@link DoubleGaugeDataPoint} instances.
     *
     * @return the size of the composite data point
     */
    int size();

    /**
     * Returns the {@link DoubleGaugeDataPoint} at the specified index.
     *
     * @param index the index of the gauge to retrieve
     * @return the {@link DoubleGaugeDataPoint} at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    DoubleGaugeDataPoint get(int index);

    /**
     * Resets all contained {@link DoubleGaugeDataPoint} instances to their initial state.
     * This default implementation iterates over all gauges and calls their reset method.
     */
    @Override
    default void reset() {
        for (int i = 0; i < size(); i++) {
            get(i).reset();
        }
    }
}
