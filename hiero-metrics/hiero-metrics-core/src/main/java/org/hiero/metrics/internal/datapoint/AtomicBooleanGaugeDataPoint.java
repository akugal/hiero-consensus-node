// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import org.hiero.metrics.api.datapoint.BooleanGaugeDataPoint;
import org.hiero.metrics.api.stat.StatUtils;

public final class AtomicBooleanGaugeDataPoint implements BooleanGaugeDataPoint {

    private final BooleanSupplier initializer;
    private final AtomicBoolean container = new AtomicBoolean();

    public AtomicBooleanGaugeDataPoint() {
        this(StatUtils.BOOL_INIT_FALSE);
    }

    public AtomicBooleanGaugeDataPoint(@NonNull BooleanSupplier initializer) {
        this.initializer = Objects.requireNonNull(initializer);
        reset();
    }

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
        container.set(initializer.getAsBoolean());
    }
}
