// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.datapoint;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.datapoint.GaugeDataPoint;

public final class AtomicReferenceGaugeDataPoint<T> implements GaugeDataPoint<T> {

    private final Supplier<T> initializer;
    private final ToDoubleFunction<T> valueConverter;
    private final AtomicReference<T> container = new AtomicReference<>();

    public AtomicReferenceGaugeDataPoint(ToDoubleFunction<T> valueConverter) {
        this(() -> null, valueConverter);
    }

    public AtomicReferenceGaugeDataPoint(Supplier<T> initializer, ToDoubleFunction<T> valueConverter) {
        this.initializer = Objects.requireNonNull(initializer, "initializer cannot be null");
        this.valueConverter = Objects.requireNonNull(valueConverter, "value converter cannot be null");
        container.set(this.initializer.get());
    }

    @Override
    public void update(T value) {
        container.set(value);
    }

    @Override
    public double getAsDouble() {
        T value = container.get();
        if (value == null) {
            return Double.NaN;
        }
        return valueConverter.applyAsDouble(container.get());
    }

    @Override
    public void reset() {
        container.set(initializer.get());
    }
}
