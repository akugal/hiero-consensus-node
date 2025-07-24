// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricsRegistrationProvider;
import org.hiero.metrics.api.utils.MetricUtils;

public class DefaultMetricRegistry implements SnapshotableMetricsRegistry {

    private static final Logger logger = LogManager.getLogger(DefaultMetricRegistry.class);

    private final List<Label> globalLabels;
    private final ConcurrentHashMap<String, Metric> metrics = new ConcurrentHashMap<>();
    private final Collection<Metric> metricsView = Collections.unmodifiableCollection(metrics.values());

    public DefaultMetricRegistry(Label... globalLabels) {
        this.globalLabels = MetricUtils.asList(globalLabels);
    }

    @NonNull
    @Override
    public List<Label> getGlobalLabels() {
        return globalLabels;
    }

    @NonNull
    @Override
    public Collection<Metric> getAll() {
        return metricsView;
    }

    @Override
    public void registerMetrics(@NonNull MetricsRegistrationProvider provider) {
        Objects.requireNonNull(provider, "metrics registration provider must not be null");

        for (Metric.Builder<?, ?> builder : provider.getMetricsToRegister()) {
            register(builder);
        }
    }

    @NonNull
    @Override
    public <M extends Metric, B extends Metric.Builder<?, M>> M getOrRegister(final @NonNull B builder) {
        return getOrCreate(builder, true);
    }

    @NonNull
    @Override
    public <M extends Metric, B extends Metric.Builder<?, M>> M register(final @NonNull B builder) {
        return getOrCreate(builder, false);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <M extends Metric> Optional<M> findMetric(@NonNull MetricKey<M> key) {
        Objects.requireNonNull(key, "metric key must not be null");
        Metric metric = metrics.get(key.getName());
        if (key.getMetricClass().isInstance(metric)) {
            return Optional.of((M) metric);
        }
        return Optional.empty();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <M extends Metric, B extends Metric.Builder<?, M>> M getOrCreate(
            @NonNull final B builder, final boolean reuseExisting) {
        Objects.requireNonNull(builder, "builder must not be null");

        final MetricKey<M> metricKey = builder.getKey();

        return (M) metrics.compute(metricKey.getName(), (name, existingMetric) -> {
            if (existingMetric != null) {
                if (reuseExisting) {
                    if (metricKey.getMetricClass().isInstance(existingMetric)) {
                        return existingMetric;
                    }
                    throw new IllegalArgumentException(
                            "Duplicate metric with same name, but different id exists. Requested key:  " + metricKey
                                    + ". Existing metric: " + existingMetric.getMetadata());
                } else {
                    throw new IllegalArgumentException("Duplicate metric name: " + metricKey + ". Existing metric: "
                            + existingMetric.getMetadata());
                }
            }

            M metric = builder.withConstantLabels(globalLabels).build();
            logger.info("Registered metric: {} with global labels: {}", metric.getMetadata(), globalLabels);
            return metric;
        });
    }
}
