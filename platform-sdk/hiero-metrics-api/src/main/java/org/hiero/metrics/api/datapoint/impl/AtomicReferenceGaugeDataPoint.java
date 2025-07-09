// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;

public final class AtomicReferenceGaugeDataPoint<T> implements GaugeDataPoint<T> {

    private final Function<T, Number> valueConverter;
    private final AtomicReference<T> container = new AtomicReference<>();

    public AtomicReferenceGaugeDataPoint(Function<T, Number> valueConverter) {
        this.valueConverter = valueConverter;
    }

    @Override
    public void update(T value) {
        container.set(value);
    }

    @Override
    public Number get() {
        return valueConverter.apply(container.get());
    }

    @Override
    public void reset() {
        container.set(null);
    }
}
