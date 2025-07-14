// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;

public final class MetricMetadata {

    private static final String EMPTY = "";

    private final MetricType metricType;
    private final String category;
    private final String name;
    private final String description;
    private final String unit;

    private final String fullName;

    public MetricMetadata(
            @NonNull MetricType metricType,
            @Nullable String category,
            @NonNull String name,
            @Nullable String description,
            @Nullable String unit) {
        this.metricType = Objects.requireNonNull(metricType, "metricType must not be null");
        this.category = category == null ? EMPTY : category.trim();
        this.name = Objects.requireNonNull(name, "name must not be null").trim();
        this.description = description == null ? EMPTY : description.trim();
        this.unit = unit == null ? EMPTY : unit.trim();

        if (this.category.isEmpty()) {
            fullName = this.name;
        } else {
            fullName = this.category + '.' + this.name;
        }
    }

    public MetricMetadata(MetricType metricType, String name) {
        this(metricType, null, name, null, null);
    }

    public MetricMetadata(MetricType metricType, String category, String name) {
        this(metricType, category, name, null, null);
    }

    public MetricType getMetricType() {
        return metricType;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    @NonNull
    public String getFullName() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || MetricMetadata.class != o.getClass()) return false;
        MetricMetadata that = (MetricMetadata) o;
        return Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
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
