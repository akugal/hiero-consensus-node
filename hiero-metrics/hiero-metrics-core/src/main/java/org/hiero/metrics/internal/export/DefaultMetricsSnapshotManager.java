// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.hiero.metrics.api.export.Exporter;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.export.PullingMetricsExporter;
import org.hiero.metrics.api.export.PushingMetricsExporter;

public class DefaultMetricsSnapshotManager extends AbstractMetricsSnapshotManager {

    private final List<PullingMetricsExporter> pullingExporters;
    private final List<PushingMetricsExporter> pushingExporters;

    private final AtomicReference<Optional<MetricsSnapshot>> snapshotHolder = new AtomicReference<>(Optional.empty());

    private final Supplier<ScheduledExecutorService> executorServiceFactory;
    private final int snapshotIntervalSeconds;
    private volatile ScheduledFuture<?> scheduledSnapshotFuture;

    public DefaultMetricsSnapshotManager(
            @NonNull Supplier<ScheduledExecutorService> executorServiceFactory,
            int snapshotIntervalSeconds,
            @NonNull List<PullingMetricsExporter> pullingExporters,
            @NonNull List<PushingMetricsExporter> pushingExporters) {
        if (snapshotIntervalSeconds <= 0) {
            throw new IllegalArgumentException("Snapshot interval must be greater than 0 seconds");
        }

        this.executorServiceFactory =
                Objects.requireNonNull(executorServiceFactory, "executor service factory must not be null");
        this.snapshotIntervalSeconds = snapshotIntervalSeconds;

        this.pullingExporters =
                List.copyOf(Objects.requireNonNull(pullingExporters, "pulling exporters must not be null"));
        this.pushingExporters =
                List.copyOf(Objects.requireNonNull(pushingExporters, "pushing exporters must not be null"));

        if (pullingExporters.isEmpty() && pushingExporters.isEmpty()) {
            throw new IllegalArgumentException("At least one pulling or pushing exporter must be configured");
        }

        logExporters("pulling", pullingExporters);
        logExporters("pushing", pushingExporters);
    }

    private void logExporters(String type, List<? extends Exporter> exporters) {
        if (exporters.isEmpty()) {
            logger.info("No {} exporters provided", type);
        } else {
            logger.info(
                    "Provided {} {} exporters: {}",
                    exporters.size(),
                    type,
                    exporters.stream().map(Exporter::getName).toList());
        }
    }

    @Override
    protected void init() {
        for (PullingMetricsExporter pullingExporter : pullingExporters) {
            try {
                logger.info("Initializing pulling exporter: {}", pullingExporter.getName());
                pullingExporter.init(snapshotHolder::get);
            } catch (RuntimeException e) {
                logger.error(
                        "Error while initializing pulling metrics exporter {}. Ignoring it",
                        pullingExporter.getName(),
                        e);
            }
        }

        logger.info("Scheduling periodic snapshotting with interval of {} seconds", snapshotIntervalSeconds);
        scheduledSnapshotFuture = executorServiceFactory
                .get()
                .scheduleAtFixedRate(new SnapshotRunnable(), 0, snapshotIntervalSeconds, TimeUnit.SECONDS);
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
