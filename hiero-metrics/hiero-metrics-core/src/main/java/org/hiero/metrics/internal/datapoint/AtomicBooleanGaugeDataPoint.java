// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.concurrent.atomic.AtomicBoolean;
import org.hiero.metrics.api.datapoint.BooleanGaugeDataPoint;

public final class AtomicBooleanGaugeDataPoint implements BooleanGaugeDataPoint {

    private final AtomicBoolean container = new AtomicBoolean();

    @Override
    public void set(boolean value) {
        container.set(value);
    }

    @Override
    public boolean getAsBoolean() {
        return container.get();
    }

    @Override
    public void reset() {
        container.set(false);
    }
}
