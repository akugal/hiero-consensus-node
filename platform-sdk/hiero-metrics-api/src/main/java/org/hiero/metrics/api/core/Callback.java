package org.hiero.metrics.api.core;

@FunctionalInterface
public interface Callback<T> {

    void call(T value, String... labelValues);
}
