package org.hiero.metrics.api.datapoint.impl;

import org.hiero.metrics.api.datapoint.BooleanGaugeDataPoint;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AtomicBooleanGaugeDataPoint implements BooleanGaugeDataPoint {

    private final AtomicBoolean container = new AtomicBoolean();

    @Override
    public void update(boolean value) {
        container.set(value);
    }

    @Override
    public boolean getAsBoolean() {
        return container.get();
    }
}
