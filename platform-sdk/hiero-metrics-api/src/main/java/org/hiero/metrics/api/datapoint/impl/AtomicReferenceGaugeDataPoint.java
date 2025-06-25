// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint.impl;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;

public final class AtomicReferenceGaugeDataPoint<T, V> implements GaugeDataPoint<T, V> {

    private final Function<T, V> valueConverter;
    private final AtomicReference<T> container = new AtomicReference<>();

    public AtomicReferenceGaugeDataPoint(Function<T, V> valueConverter) {
        this.valueConverter = valueConverter;
    }

    @Override
    public void update(T value) {
        container.set(value);
    }

    @Override
    public V get() {
        return valueConverter.apply(container.get());
    }
}
