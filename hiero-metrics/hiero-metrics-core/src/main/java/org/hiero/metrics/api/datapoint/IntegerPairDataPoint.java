// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.DoubleSupplier;

public interface IntegerPairDataPoint extends DoubleSupplier, DataPoint {

    void update(int left, int right);

    int getLeft();

    int getRight();

    double getAndReset();
}
