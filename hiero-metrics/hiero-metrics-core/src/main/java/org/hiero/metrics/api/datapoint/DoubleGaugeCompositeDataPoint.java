// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

public interface DoubleGaugeCompositeDataPoint extends DataPoint {

    void update(double value);

    int size();

    DoubleGaugeDataPoint get(int index);

    @Override
    default void reset() {
        for (int i = 0; i < size(); i++) {
            get(i).reset();
        }
    }
}
