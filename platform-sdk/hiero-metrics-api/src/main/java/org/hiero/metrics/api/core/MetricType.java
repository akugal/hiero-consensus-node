// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

public enum MetricType {
    COUNTER("counter"),
    GAUGE("gauge"),
    INFO("info");

    private final String name;

    MetricType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
