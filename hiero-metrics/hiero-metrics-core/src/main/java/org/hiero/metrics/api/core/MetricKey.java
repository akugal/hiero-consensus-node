// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;

public final class MetricKey<M extends Metric> {

    private final String name;
    private final Class<M> metricClass;

    private MetricKey(@NonNull String name, @NonNull Class<M> metricClass) {
        this.name = Objects.requireNonNull(name);
        this.metricClass = Objects.requireNonNull(metricClass);
    }

    @SuppressWarnings("unchecked")
    public static <M extends Metric> MetricKey<M> of(@NonNull String name, @NonNull Class<? super M> metricClass) {
        return new MetricKey<>(name, (Class<M>) metricClass);
    }

    @SuppressWarnings("unchecked")
    public static <M extends Metric> MetricKey<M> of(
            @NonNull String category, @NonNull String name, @NonNull Class<? super M> metricClass) {
        ArgumentUtils.throwArgBlank(category, "category");
        return new MetricKey<>(category + "." + name, (Class<M>) metricClass);
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Class<M> getMetricClass() {
        return metricClass;
    }

    @Override
    public String toString() {
        return "MetricKey{" + "name='" + name + '\'' + ", metricClass=" + metricClass + '}';
    }
}
