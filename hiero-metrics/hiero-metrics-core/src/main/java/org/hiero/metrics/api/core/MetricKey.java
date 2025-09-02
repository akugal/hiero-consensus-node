// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;

/**
 * A unique key for identifying a {@link Metric} by its name and type.
 * Key instance is immutable and can be used to retrieve a metric from a {@link MetricRegistry}.
 *
 * @param <M> the type of the metric
 */
public record MetricKey<M extends Metric>(@NonNull String name, @NonNull Class<M> type) {

    /**
     * Constructs a new metric key instance with the specified name and type.
     *
     * @param name the name of the metric, must not be blank
     * @param type the class type of the metric, must not be null
     */
    public MetricKey(@NonNull String name, @NonNull Class<M> type) {
        this.name = ArgumentUtils.throwArgBlank(name, "metric name");
        this.type = Objects.requireNonNull(type, "metric type must not be null");
    }

    /**
     * Creates a new metric key instance with the specified name and type.
     *
     * @param name the name of the metric, must not be blank
     * @param type the class type of the metric, must not be null
     * @param <M>  the type of the metric
     * @return a new metric key instance
     */
    @SuppressWarnings("unchecked")
    public static <M extends Metric> MetricKey<M> of(@NonNull String name, @NonNull Class<? super M> type) {
        return new MetricKey<>(name, (Class<M>) type);
    }

    /**
     * Returns a new metric key instance with the specified category prefixed to the metric name.
     * The category and name are separated by a colon (':').
     *
     * @param category the category to prefix to the metric name, must not be blank
     * @return a new metric key instance with the category prefixed to the name
     */
    public MetricKey<M> withCategory(@NonNull String category) {
        ArgumentUtils.throwArgBlank(category, "category");
        return new MetricKey<>(category + ':' + name, type);
    }
}
