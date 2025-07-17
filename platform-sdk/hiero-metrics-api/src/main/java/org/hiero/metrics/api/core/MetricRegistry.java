// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hiero.metrics.internal.core.DefaultMetricRegistry;

public interface MetricRegistry {

    MetricRegistry DEFAULT = new DefaultMetricRegistry();

    static MetricRegistry create(@NonNull String name, Label... globalLabels) {
        return new DefaultMetricRegistry(name, globalLabels);
    }

    @NonNull
    String getName();

    @NonNull
    List<Label> getGlobalLabels();

    @NonNull
    Collection<Metric> getAll();

    @NonNull
    <M extends Metric, B extends Metric.Builder<?, M>> M register(B builder);

    @NonNull
    <M extends Metric> Optional<M> findMetric(String name);

    @NonNull
    default <M extends Metric> M getMetric(String name) {
        Optional<M> metric = findMetric(name);
        if (metric.isPresent()) {
            return metric.get();
        }
        throw new IllegalArgumentException("Metric not found: " + name);
    }

    default void reset() {
        getAll().forEach(Metric::reset);
    }
}
