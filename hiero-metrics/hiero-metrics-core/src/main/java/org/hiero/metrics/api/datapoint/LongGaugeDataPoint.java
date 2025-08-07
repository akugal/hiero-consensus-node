// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.LongSupplier;

public interface LongGaugeDataPoint extends LongSupplier, DataPoint {

    long getInitValue();

    default void increment() {
        update(1L);
    }

    default void decrement() {
        update(-1L);
    }

    void update(long value);

    long getAndReset();

    @Override
    default void reset() {
        getAndReset();
    }
}
