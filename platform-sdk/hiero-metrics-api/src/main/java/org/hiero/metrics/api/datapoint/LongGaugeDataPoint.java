// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.LongSupplier;

public interface LongGaugeDataPoint extends LongSupplier, DataPoint {

    LongSupplier DEFAULT_INIT = () -> 0L;

    long getInitValue();

    void update(long value);

    long getAndReset();

    @Override
    default void reset() {
        getAndReset();
    }
}
