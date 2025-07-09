// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

@FunctionalInterface
public interface MetricCallback<T extends Number> {

    void call(T value, String... labelValues);
}
