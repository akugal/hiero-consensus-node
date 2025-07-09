// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.MetricUtils;

public interface DoubleGaugeDataPoint extends DoubleSupplier, DataPoint {

    DoubleSupplier DEFAULT_INIT = () -> MetricUtils.ZERO;

    double getInitValue();

    void update(double value);

    double getAndReset();

    @Override
    default void reset() {
        getAndReset();
    }
}
