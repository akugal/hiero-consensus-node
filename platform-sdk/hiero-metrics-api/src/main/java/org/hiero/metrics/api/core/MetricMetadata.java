// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.Objects;

// TODO format?
public final class MetricMetadata {

    private final MetricType metricType;
    private final String category;
    private final String name;
    private final String description;
    private final String unit;

    private final String fullName;
    private final int hashCode;

    public MetricMetadata(MetricType metricType, String category, String name, String description, String unit) {
        this.metricType = metricType;
        this.category = category == null ? "" : category.trim();
        this.name = Objects.requireNonNull(name, "name must not be null").trim();
        this.description = description == null ? "" : description.trim();
        this.unit = unit == null ? "" : unit.trim();

        if (this.category.isEmpty()) {
            fullName = this.name;
            hashCode = this.name.hashCode();
        } else {
            fullName = this.category + '.' + this.name;
            hashCode = Objects.hash(this.category, this.name);
        }
    }

    public MetricMetadata(MetricType metricType, String name) {
        this(metricType, "", name, "", "");
    }

    public MetricMetadata(MetricType metricType, String category, String name) {
        this(metricType, category, name, "", "");
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || MetricMetadata.class != o.getClass()) return false;
        MetricMetadata that = (MetricMetadata) o;
        return Objects.equals(category, that.category) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "MetricMetadata{" + "metricType="
                + metricType + ", category='"
                + category + '\'' + ", name='"
                + name + '\'' + ", description='"
                + description + '\'' + ", unit='"
                + unit + '\'' + '}';
    }
}
