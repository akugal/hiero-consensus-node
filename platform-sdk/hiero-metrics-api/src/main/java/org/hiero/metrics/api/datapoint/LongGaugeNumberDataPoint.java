// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import java.util.function.Supplier;

public interface LongGaugeNumberDataPoint extends Supplier<Number> {

    long getInitValue();

    void update(long value);

    long getAndReset();
}
