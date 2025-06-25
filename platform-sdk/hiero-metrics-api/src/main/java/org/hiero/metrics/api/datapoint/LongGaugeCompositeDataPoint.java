// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

public interface LongGaugeCompositeDataPoint {

    void update(long value);

    int size();

    LongGaugeDataPoint get(int index);
}
