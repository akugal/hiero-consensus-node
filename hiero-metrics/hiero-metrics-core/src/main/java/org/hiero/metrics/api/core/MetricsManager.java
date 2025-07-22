// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import static org.hiero.metrics.api.utils.MetricUtils.load;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.hiero.metrics.api.snapshot.PullingMetricsExporter;
import org.hiero.metrics.api.snapshot.PushingMetricsExporter;
import org.hiero.metrics.internal.core.DefaultMetricsManager;

public interface MetricsManager {

    class DefaultHolder {
        private static final MetricsManager INSTANCE =
                createWithDiscoveredExporters(Executors.newSingleThreadScheduledExecutor(), 2);
    }

    static MetricsManager getDefault() {
        return DefaultHolder.INSTANCE;
    }

    static MetricsManager createWithDiscoveredExporters(
            @NonNull ScheduledExecutorService executorService, int snapshotIntervalSeconds) {
        return new DefaultMetricsManager(
                executorService,
                snapshotIntervalSeconds,
                load(PullingMetricsExporter.class),
                load(PushingMetricsExporter.class));
    }

    static MetricsManager createSimple(PushingMetricsExporter exporter, int snapshotIntervalSeconds) {
        return new DefaultMetricsManager(
                Executors.newSingleThreadScheduledExecutor(), snapshotIntervalSeconds, List.of(), List.of(exporter));
    }

    static MetricsManager createSimple(PullingMetricsExporter exporter) {
        return new DefaultMetricsManager(
                Executors.newSingleThreadScheduledExecutor(), -1, List.of(exporter), List.of());
    }

    void manageMetricsRegistry(MetricRegistry registry);

    @NonNull
    MetricRegistry createManagedMetricsRegistry(String name, Label... globalLabels);

    void resetAll();

    boolean hasRunningSnapshotThread();

    void shutdown();
}
