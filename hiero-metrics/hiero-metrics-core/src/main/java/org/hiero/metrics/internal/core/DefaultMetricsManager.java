// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsManager;
import org.hiero.metrics.api.snapshot.Exporter;
import org.hiero.metrics.api.snapshot.MetricSnapshot;
import org.hiero.metrics.api.snapshot.MetricsSnapshot;
import org.hiero.metrics.api.snapshot.PullingMetricsExporter;
import org.hiero.metrics.api.snapshot.PushingMetricsExporter;

// TODO add metrics about snapshotting, exporters, registries, etc.
public class DefaultMetricsManager implements MetricsManager {

    private static final Logger logger = LogManager.getLogger(DefaultMetricsManager.class);

    private final Set<String> registryNames = new HashSet<>();
    private final List<MetricRegistry> metricRegistries = new CopyOnWriteArrayList<>();
    // private final List<Label> globalLabels;

    private final List<PullingMetricsExporter> pullingExporters;
    private final List<PushingMetricsExporter> pushingExporters;

    private final AtomicReference<Optional<MetricsSnapshot>> snapshotHolder = new AtomicReference<>(Optional.empty());

    private final ScheduledExecutorService executorService;
    private final int snapshotIntervalSeconds;
    private volatile ScheduledFuture<?> scheduledSnapshotFuture;

    public DefaultMetricsManager(
            @NonNull ScheduledExecutorService executorService,
            int snapshotIntervalSeconds,
            @NonNull List<PullingMetricsExporter> pullingExporters,
            @NonNull List<PushingMetricsExporter> pushingExporters) {
        this.executorService = Objects.requireNonNull(executorService, "executorService must not be null");
        this.snapshotIntervalSeconds = snapshotIntervalSeconds;

        this.pullingExporters = Objects.requireNonNull(pullingExporters, "pulling exporters must not be null");
        this.pushingExporters = Objects.requireNonNull(pushingExporters, "pushing exporters must not be null");

        logExporters("pulling", pullingExporters);
        logExporters("pushing", pushingExporters);
    }

    private void logExporters(String type, List<? extends Exporter> exporters) {
        if (exporters.isEmpty()) {
            logger.info("No {} exporters configured", type);
        } else {
            logger.info(
                    "Configured {} {} exporters: {}",
                    exporters.size(),
                    type,
                    exporters.stream().map(Exporter::getName).collect(Collectors.toList()));
        }
    }

    private void init() {
        if (pushingExporters.isEmpty() && pullingExporters.isEmpty()) {
            return;
        }

        if (pushingExporters.isEmpty() && pullingExporters.size() == 1) {
            // if there is only one pulling exporter, we can use it directly without scheduling
            pullingExporters.getFirst().init(this::takeSnapshot);
            logger.info("Only one pulling exporter configured, using it snapshots directly");
        } else {
            for (PullingMetricsExporter pullingExporter : pullingExporters) {
                try {
                    pullingExporter.init(snapshotHolder::get);
                } catch (RuntimeException e) {
                    logger.error(
                            "Error while initializing pulling metrics exporter {}. Ignoring it",
                            pullingExporter.getName(),
                            e);
                }
            }

            logger.info("Scheduling periodic snapshotting with interval {} seconds", snapshotIntervalSeconds);
            scheduledSnapshotFuture = executorService.scheduleAtFixedRate(
                    new SnapshotRunnable(), 0, snapshotIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public void manageMetricsRegistry(MetricRegistry registry) {
        boolean firstRegistry = false;

        synchronized (registryNames) {
            if (!registryNames.add(registry.getName())) {
                throw new IllegalArgumentException(
                        "Metric registry with name '" + registry.getName() + "' already exists.");
            }

            if (registryNames.size() == 1) {
                firstRegistry = true;
            }
        }

        metricRegistries.add(registry);
        logger.info("Added managed metrics registry: {}", registry.getName());

        if (firstRegistry) {
            init();
        }
    }

    @NonNull
    @Override
    public MetricRegistry createManagedMetricsRegistry(String name, Label... globalLabels) {
        MetricRegistry registry = MetricRegistry.create(name, globalLabels);
        manageMetricsRegistry(registry);
        return registry;
    }

    // returns immutable list of snapshots
    @NonNull
    private Optional<MetricsSnapshot> takeSnapshot() {
        final List<MetricSnapshot> snapshots;

        if (metricRegistries.isEmpty()) {
            return Optional.empty();
        } else if (metricRegistries.size() == 1) {
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

        return Optional.of(new MetricsSnapshot(snapshots, Instant.now()));
    }

    @Override
    public void resetAll() {
        for (MetricRegistry registry : metricRegistries) {
            registry.reset();
        }
    }

    @Override
    public boolean hasRunningSnapshotThread() {
        return scheduledSnapshotFuture != null;
    }

    @Override
    public synchronized void shutdown() {
        if (scheduledSnapshotFuture != null && !scheduledSnapshotFuture.isDone()) {
            scheduledSnapshotFuture.cancel(false);
            scheduledSnapshotFuture = null;

            // just in case - re-init pulling exporters to get empty snapshots
            for (PullingMetricsExporter pullingExporter : pullingExporters) {
                try {
                    pullingExporter.init(Optional::empty);
                } catch (RuntimeException ex) {
                    logger.error(
                            "Error while de-initializing pulling metrics exporter {}", pullingExporter.getName(), ex);
                    // ignore, we are stopping anyway
                }
            }
        }
    }

    private class SnapshotRunnable implements Runnable {

        @Override
        public void run() {
            final Optional<MetricsSnapshot> snapshotOptional = takeSnapshot();
            snapshotHolder.set(snapshotOptional);

            if (snapshotOptional.isEmpty()) {
                return;
            }

            final MetricsSnapshot snapshot = snapshotOptional.get();
            for (PushingMetricsExporter pushingExporter : pushingExporters) {
                try {
                    pushingExporter.export(snapshot);
                } catch (IOException ex) {
                    // TODO disable and enable back after some time, completely remove after some time in disabled state
                    logger.error(
                            "Error while exporting metrics snapshot by pushing metrics exporter {}",
                            pushingExporter.getName(),
                            ex);
                } catch (RuntimeException ex) {
                    // TODO remove from pushing exporters list
                    logger.error(
                            "Error while exporting metrics snapshot by pushing metrics exporter {}",
                            pushingExporter.getName(),
                            ex);
                }
            }
        }
    }
}
