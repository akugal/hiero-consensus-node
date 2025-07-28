// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;

public interface DoubleGaugeDataPoint extends DoubleSupplier, DataPoint {

    double getInitValue();

    void update(double value);

    double getAndReset();
}
