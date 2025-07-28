// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.BooleanSupplier;

public interface BooleanGaugeDataPoint extends BooleanSupplier, DataPoint {

    void set(boolean value);

    default void setTrue() {
        set(true);
    }

    default void setFalse() {
        set(false);
    }
}
