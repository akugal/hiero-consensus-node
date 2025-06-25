// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

@FunctionalInterface
public interface Callback<T> {

    void call(T value, String... labelValues);
}
