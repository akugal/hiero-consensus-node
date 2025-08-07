// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.stat.StatUtils;

public interface DoubleGaugeDataPoint extends DoubleSupplier, DataPoint {

    double getInitValue();

    default void update() {
        update(StatUtils.ONE);
    }

    void update(double value);

    double getAndReset();
}
