// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;

public final class MetricMetadata {

    private static final String EMPTY = "";

    private final MetricType metricType;
    private final String name;
    private final String description;
    private final String unit;

    private final int hashCode;

    public MetricMetadata(
            @NonNull MetricType metricType, @NonNull String name, @Nullable String description, @Nullable String unit) {
        this.metricType = Objects.requireNonNull(metricType, "metricType must not be null");
        this.name = ArgumentUtils.throwArgBlank(name, "name");
        this.description = description == null ? EMPTY : description;
        this.unit = unit == null ? EMPTY : unit;

        hashCode = Objects.hash(metricType, name, description, unit);
    }

    public MetricMetadata(MetricType metricType, String name) {
        this(metricType, name, null, null);
    }

    public MetricType getMetricType() {
        return metricType;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MetricMetadata that = (MetricMetadata) o;
        return metricType == that.metricType
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "MetricMetadata{" + "metricType="
                + metricType + ", name='"
                + name + '\'' + ", description='"
                + description + '\'' + ", unit='"
                + unit + '\'' + '}';
    }
}
