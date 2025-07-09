// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.Supplier;

public interface GaugeDataPoint<T> extends Supplier<Number>, DataPoint {

    void update(T value);
}
