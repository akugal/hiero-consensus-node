// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MetricRegistry {

    @NonNull
    List<Label> getGlobalLabels();

    @NonNull
    Collection<Metric> getAll();

    void registerMetrics(@NonNull MetricsRegistrationProvider provider);

    @NonNull
    <M extends Metric, B extends Metric.Builder<?, M>> M register(@NonNull B builder);

    @NonNull
    <M extends Metric> Optional<M> findMetric(@NonNull MetricKey<M> key);

    @NonNull
    default <M extends Metric> M getMetric(@NonNull MetricKey<M> key) {
        Optional<M> metric = findMetric(key);
        if (metric.isPresent()) {
            return metric.get();
        }
        throw new IllegalArgumentException("Metric not found: " + key);
    }

    default void reset() {
        getAll().forEach(Metric::reset);
    }
}
