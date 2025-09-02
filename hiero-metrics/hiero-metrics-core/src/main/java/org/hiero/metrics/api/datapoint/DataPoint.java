// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

/**
 * Base interface for a metric data point that holds a measurement values(s). It can be associated with
 * a set of dynamic labels (key-value pairs) that provide additional context for the measurement.
 * <p>
 * Data points are mutable and can be updated with new measurements. They can also be reset to their initial state.
 * Implementations are expected to be thread-safe and handle concurrent updates atomically.
 */
public interface DataPoint {

    /**
     * Resets the data point to its initial state.
     * Implementations should ensure that any internal state is cleared or set back to default values.
     */
    void reset();
}
