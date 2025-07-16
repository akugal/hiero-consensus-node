// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.core.snapshot.MetricSnapshot;
import org.hiero.metrics.api.export.api.PullingMetricsExporter;
import org.hiero.metrics.api.export.api.PushingMetricsExporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

//TODO add registry and metrics about snapshotting, exporters, etc.
public class MetricsManager implements AutoCloseable {

    private final List<MetricRegistry> metricRegistries = new CopyOnWriteArrayList<>();
    private final Set<Set<Label>> globalLabelsSet = new HashSet<>();

    private final List<PullingMetricsExporter> pullingExporters;
    private final List<PushingMetricsExporter> pushingExporters;

    private final ScheduledFuture<?> scheduledSnapshotFuture;

    public MetricsManager() {
        this(Executors.newSingleThreadScheduledExecutor(), 3);
    }

    public MetricsManager(int snapshotIntervalSeconds) {
        this(Executors.newSingleThreadScheduledExecutor(), snapshotIntervalSeconds);
    }

    public MetricsManager(@NonNull ScheduledExecutorService executorService, int snapshotIntervalSeconds) {
        this(executorService, snapshotIntervalSeconds,
                loadExporters(PullingMetricsExporter.class), loadExporters(PushingMetricsExporter.class));
    }

    public MetricsManager(@NonNull ScheduledExecutorService executorService, int snapshotIntervalSeconds,
                          @NonNull List<PullingMetricsExporter> pullingExporters,
                          @NonNull List<PushingMetricsExporter> pushingExporters) {
        Objects.requireNonNull(executorService, "executorService must not be null");

        this.pullingExporters = Objects.requireNonNull(pullingExporters, "pullingExporters must not be null");
        this.pushingExporters = Objects.requireNonNull(pushingExporters, "pushingExporters must not be null");

        scheduledSnapshotFuture = init(executorService, snapshotIntervalSeconds);
    }

    private static <E> List<E> loadExporters(Class<E> exporterClass) {
        ServiceLoader<E> pullingExportersLoader = ServiceLoader.load(exporterClass);
        return pullingExportersLoader.stream().map(ServiceLoader.Provider::get).toList();
    }

    public MetricRegistry createMetricRegistry(Label... globalLabels) {
        if (!globalLabelsSet.add(Set.of(globalLabels))) {
            throw new IllegalArgumentException("Global labels must be unique across all metric registries. " +
                    "Found duplicate: " + Arrays.toString(globalLabels));
        }

        MetricRegistry registry = new MetricRegistry(globalLabels);
        metricRegistries.add(registry);
        return registry;
    }

    private ScheduledFuture<?> init(ScheduledExecutorService executorService, int snapshotIntervalSeconds) {
        if (pushingExporters.isEmpty() && pullingExporters.isEmpty()) {
            return null;
        }

        if (pushingExporters.isEmpty() && pullingExporters.size() == 1) {
            pullingExporters.getFirst().init(this::snapshot);
            return null;
        } else {
            SnapshotProvider snapshotProvider = new SnapshotProvider();

            for (PullingMetricsExporter pullingExporter : pullingExporters) {
                pullingExporter.init(snapshotProvider);
            }

            return executorService.scheduleAtFixedRate(snapshotProvider, 0, snapshotIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    private List<MetricSnapshot> snapshot() {
        if (metricRegistries.isEmpty()) {
            return List.of();
        } else if (metricRegistries.size() == 1) {
            return metricRegistries.getFirst().snapshot();
        } else {
            List<MetricSnapshot> snapshots = new ArrayList<>();
            for (MetricRegistry registry : metricRegistries) {
                snapshots.addAll(registry.snapshot());
            }
            snapshots.sort(MetricSnapshot.COMPARATOR);
            return snapshots;
        }
    }

    public void stop() {
        if (scheduledSnapshotFuture != null && !scheduledSnapshotFuture.isDone()) {
            scheduledSnapshotFuture.cancel(false);

            // allow pulling exporters to continue taking snapshot on demand without sync
            try {
                for (PullingMetricsExporter pullingExporter : pullingExporters) {
                    pullingExporter.init(this::snapshot);
                }
            } catch (RuntimeException ex) {
                // ignore, we are stopping anyway
            }
        }
    }

    public void resetAll() {
        for (MetricRegistry registry : metricRegistries) {
            registry.reset();
        }
    }

    @Override
    public void close() {
        stop();
    }

    private class SnapshotProvider implements Runnable, Supplier<List<MetricSnapshot>> {

        private final AtomicReference<List<MetricSnapshot>> snapshotsRef = new AtomicReference<>(List.of());

        @Override
        public List<MetricSnapshot> get() {
            return snapshotsRef.get();
        }

        @Override
        public void run() {
            List<MetricSnapshot> snapshot = snapshot();
            snapshotsRef.set(snapshot);

            for (PushingMetricsExporter pushingExporter : pushingExporters) {
                try {
                    pushingExporter.export(snapshot);
                } catch (IOException ex) {
                    // TODO disable and enable back after some time, completely remove after some time in disabled state
                    ex.printStackTrace();
                } catch (RuntimeException ex) {
                    // TODO remove from pushing exporters list
                    ex.printStackTrace();
                }
            }
        }
    }
}
