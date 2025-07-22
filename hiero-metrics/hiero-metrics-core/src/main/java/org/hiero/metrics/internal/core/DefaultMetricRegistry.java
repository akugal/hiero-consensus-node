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
import org.hiero.metrics.api.utils.MetricUtils;

public class DefaultMetricRegistry implements SnapshotableMetricsRegistry {

    private static final Logger logger = LogManager.getLogger(DefaultMetricRegistry.class);

    private final String name;
    private final List<Label> globalLabels;
    private final ConcurrentHashMap<String, Metric> metrics = new ConcurrentHashMap<>();
    private final Collection<Metric> metricsView = Collections.unmodifiableCollection(metrics.values());

    public DefaultMetricRegistry(Label... globalLabels) {
        this("", globalLabels);
    }

    public DefaultMetricRegistry(@NonNull String name, Label... globalLabels) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.globalLabels = MetricUtils.asList(globalLabels);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
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

    @NonNull
    @Override
    public <M extends Metric, B extends Metric.Builder<?, M>> M register(B builder) {
        if (!name.isEmpty()) {
            builder.withName(name + ':' + builder.getName());
        }
        builder.withConstantLabels(globalLabels);
        M metric = builder.build();

        Metric prev = metrics.putIfAbsent(builder.getName(), metric);

        if (prev != null) {
            // TODO why we should fail if we can reduce failures by just logging conflict and use latest registered ?
            throw new IllegalArgumentException(
                    "Duplicate metric registration: " + builder.getName() + ". Existing metric: " + prev.getMetadata());
        }

        logger.info("Registered metric: {} with global labels: {}", metric.getMetadata(), globalLabels);
        return metric;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <M extends Metric> Optional<M> findMetric(String name) {
        return Optional.ofNullable((M) metrics.get(name));
    }
}
