// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

// TODO format?
public record MetricMetadata(String category, String name, String description, String unit) {

    public MetricMetadata(String name) {
        this("", name, "", "");
    }

    public MetricMetadata(String category, String name) {
        this(category, name, "", "");
    }
}
