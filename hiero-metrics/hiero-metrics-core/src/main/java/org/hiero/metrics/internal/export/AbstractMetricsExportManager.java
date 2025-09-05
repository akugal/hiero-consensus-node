// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.LongGauge;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.api.export.MetricsExportManager;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.utils.Unit;
import org.hiero.metrics.internal.core.SnapshotableMetricsRegistry;

public abstract class AbstractMetricsExportManager implements MetricsExportManager {

    protected static final Logger logger = LogManager.getLogger(MetricsExportManager.class);

    private final String name;
    private final Set<Set<Label>> registriesGlobalLabels = new HashSet<>();
    private final List<MetricRegistry> metricRegistries = new ArrayList<>();

    private SnapshotableMetricsRegistry exportMetricsRegistry;
    private LongGaugeDataPoint snapshotDurationMetric;

    protected AbstractMetricsExportManager(@NonNull String name) {
        this.name = ArgumentUtils.throwArgBlank(name, "name");
    }

    @NonNull
    @Override
    public final String name() {
        return name;
    }

    /**
     * Initializes the manager.
     * Called only once when first metrics registry is managed by this manager
     * when {@link #manageMetricRegistry(MetricRegistry)} is called.
     */
    protected void init() {
        exportMetricsRegistry =
                (SnapshotableMetricsRegistry) MetricsFacade.createRegistry(new Label("export_manager", name));
        registerExportMetrics("export", exportMetricsRegistry);
    }

    /**
     * Register export metrics.
     *
     * @param category category for export mentrics to be used
     * @param exportMetricsRegistry registry to register export metrics with
     */
    protected void registerExportMetrics(@NonNull String category, @NonNull MetricRegistry exportMetricsRegistry) {
        snapshotDurationMetric = exportMetricsRegistry
                .register(LongGauge.builder(LongGauge.key("snapshot_duration").withCategory(category))
                        .withDescription("Snapshot duration time in milliseconds")
                        .withUnit(Unit.MILLISECOND_UNIT))
                .getNotLabeled();
    }

    @Override
    public final void manageMetricRegistry(@NonNull MetricRegistry registry) {
        boolean firstRegistry = false;
        HashSet<Label> globalLabels = new HashSet<>(registry.globalLabels());

        synchronized (this) {
            if (!registriesGlobalLabels.add(globalLabels)) {
                throw new IllegalArgumentException(
                        "Metric registry has duplicate global labels with another registry: " + globalLabels);
            }

            if (registriesGlobalLabels.size() == 1) {
                firstRegistry = true;
            }

            metricRegistries.add(registry);
        }

        logger.info("Added metrics registry with global labels: {}", globalLabels);

        if (firstRegistry) {
            init();
        }
    }

    // returns immutable list of snapshots
    @NonNull
    protected synchronized final Optional<MetricsSnapshot> takeSnapshot() {
        if (metricRegistries.isEmpty()) {
            return Optional.empty();
        }

        final List<MetricSnapshot> snapshots = new ArrayList<>();
        final long startTime = System.currentTimeMillis();

        if (metricRegistries.size() == 1) {
            if (metricRegistries.getFirst() instanceof SnapshotableMetricsRegistry snapshotable) {
                snapshots.addAll(snapshotable.snapshot());
            } else {
                return Optional.empty();
            }
        } else {
            for (MetricRegistry registry : metricRegistries) {
                if (registry instanceof SnapshotableMetricsRegistry snapshotable) {
                    snapshots.addAll(snapshotable.snapshot());
                }
            }
        }

        // snapshotDurationMetric should be not null when at least one metrics registry is managed
        long durationMs = System.currentTimeMillis() - startTime;
        snapshotDurationMetric.update(durationMs);
        snapshots.addAll(exportMetricsRegistry.snapshot());

        return Optional.of(new MetricsSnapshot(Collections.unmodifiableList(snapshots), Instant.now()));
    }

    @Override
    public final void resetAll() {
        metricRegistries.forEach(MetricRegistry::reset);
    }

    @Override
    public void shutdown() {
        // nothing to do by default
    }
}
