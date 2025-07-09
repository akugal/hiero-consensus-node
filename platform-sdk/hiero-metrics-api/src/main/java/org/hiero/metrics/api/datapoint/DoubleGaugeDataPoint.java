// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import static org.hiero.metrics.api.core.MetricUtils.ZERO;

import java.util.function.DoubleSupplier;

public interface DoubleGaugeDataPoint extends DoubleSupplier, DataPoint {

    DoubleSupplier DEFAULT_INIT = () -> ZERO;

    double getInitValue();

    void update(double value);

    double getAndReset();

    @Override
    default void reset() {
        getAndReset();
    }
}
