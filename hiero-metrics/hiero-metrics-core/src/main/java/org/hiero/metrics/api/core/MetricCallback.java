// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

@FunctionalInterface
public interface MetricCallback {

    void call(double value, String... labelValues);
}
