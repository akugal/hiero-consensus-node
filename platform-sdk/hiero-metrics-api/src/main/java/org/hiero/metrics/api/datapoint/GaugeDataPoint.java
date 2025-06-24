package org.hiero.metrics.api.datapoint;

import java.util.function.Supplier;

public interface GaugeDataPoint<T, V> extends Supplier<V> {

    void update(T value);
}