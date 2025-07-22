// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;

public interface GaugeDataPoint<T> extends DoubleSupplier, DataPoint {

    void update(T value);
}
