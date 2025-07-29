// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.export.MetricsSnapshotManager;
import org.hiero.metrics.internal.core.SnapshotableMetricsRegistry;

public abstract class AbstractMetricsSnapshotManager implements MetricsSnapshotManager {

    protected static final Logger logger = LogManager.getLogger(MetricsSnapshotManager.class);

    private final Set<Set<Label>> registriesGlobalLabels = new HashSet<>();
    private final List<MetricRegistry> metricRegistries = new CopyOnWriteArrayList<>();

    protected abstract void init();

    @Override
    public final void manageMetricRegistry(@NonNull MetricRegistry registry) {
        boolean firstRegistry = false;
        HashSet<Label> globalLabels = new HashSet<>(registry.getGlobalLabels());

        synchronized (registriesGlobalLabels) {
            if (!registriesGlobalLabels.add(globalLabels)) {
                throw new IllegalArgumentException(
                        "Metric registry has duplicate global labels with another registry: " + globalLabels);
            }

            if (registriesGlobalLabels.size() == 1) {
                firstRegistry = true;
            }
        }

        metricRegistries.add(registry);
        logger.info("Added metrics registry with global labels: {}", globalLabels);

        if (firstRegistry) {
            init();
        }
    }

    // returns immutable list of snapshots
    @NonNull
    protected final Optional<MetricsSnapshot> takeSnapshot() {
        final List<MetricSnapshot> snapshots;

        if (metricRegistries.size() == 1) {
            if (metricRegistries.getFirst() instanceof SnapshotableMetricsRegistry snapshotable) {
                snapshots = Collections.unmodifiableList(snapshotable.snapshot());
            } else {
                return Optional.empty();
            }
        } else {
            snapshots = metricRegistries.stream()
                    .filter(SnapshotableMetricsRegistry.class::isInstance)
                    .map(SnapshotableMetricsRegistry.class::cast)
                    .flatMap(registry -> registry.snapshot().stream())
                    .sorted(MetricSnapshot.COMPARATOR)
                    .toList(); // immutable list
        }

        if (snapshots.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new MetricsSnapshot(snapshots, Instant.now()));
    }

    @Override
    public final void resetAll() {
        for (MetricRegistry registry : metricRegistries) {
            registry.reset();
        }
    }

    @Override
    public void shutdown() {
        // nothing to do by default
    }
}
