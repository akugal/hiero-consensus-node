// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import static org.hiero.metrics.api.utils.MetricUtils.load;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.snapshot.MetricsSnapshotManager;
import org.hiero.metrics.api.snapshot.PullingMetricsExporter;
import org.hiero.metrics.api.snapshot.PushingMetricsExporter;
import org.hiero.metrics.api.utils.MetricUtils;
import org.hiero.metrics.internal.core.DefaultMetricRegistry;
import org.hiero.metrics.internal.snapshot.DefaultMetricsSnapshotManager;
import org.hiero.metrics.internal.snapshot.NoOpMetricsSnapshotManager;
import org.hiero.metrics.internal.snapshot.SinglePullingExporterMetricsSnapshotManager;

public final class MetricsFacade {

    private static final Logger logger = LogManager.getLogger(MetricsFacade.class);

    private static final class DefaultSnapshotManagerHolder {
        private static final MetricsSnapshotManager INSTANCE =
                createSnapshotManagerWithDiscoveredExporters(Executors::newSingleThreadScheduledExecutor, 1);
    }

    private MetricsFacade() {
        // Prevent instantiation
    }

    public static MetricRegistry createRegistry(Label... globalLabels) {
        return new DefaultMetricRegistry(globalLabels);
    }

    public static MetricRegistry createRegistryWithDiscoveredProviders(Label... globalLabels) {
        List<MetricsRegistrationProvider> providers = MetricUtils.load(MetricsRegistrationProvider.class);
        MetricRegistry registry = createRegistry(globalLabels);

        if (providers.isEmpty()) {
            logger.info("No metrics registration providers found. Creating empty registry.");
            return registry;
        }

        for (MetricsRegistrationProvider provider : providers) {
            logger.info("Registering metrics from provider: {}", provider.getClass());
            registry.registerMetrics(provider);
        }

        return new DefaultMetricRegistry(globalLabels);
    }

    public static MetricsSnapshotManager getDefaultSnapshotManager() {
        return DefaultSnapshotManagerHolder.INSTANCE;
    }

    public static MetricsSnapshotManager createSnapshotManagerWithDiscoveredExporters(
            @NonNull Supplier<ScheduledExecutorService> executorServiceFactory, int snapshotIntervalSeconds) {
        List<PullingMetricsExporter> pullingExporters = load(PullingMetricsExporter.class);
        List<PushingMetricsExporter> pushingExporters = load(PushingMetricsExporter.class);

        if (pullingExporters.isEmpty() && pushingExporters.isEmpty()) {
            logger.info("No metrics exporters found. Using no-op snapshot manager.");
            return NoOpMetricsSnapshotManager.INSTANCE;
        }

        if (pushingExporters.isEmpty() && pullingExporters.size() == 1) {
            logger.info("Single pulling exporter found. No snapshot thread will be running.");
            return new SinglePullingExporterMetricsSnapshotManager(pullingExporters.getFirst());
        }

        return new DefaultMetricsSnapshotManager(
                executorServiceFactory, snapshotIntervalSeconds, pullingExporters, pushingExporters);
    }

    public static MetricsSnapshotManager createSnapshotManager(
            @NonNull PushingMetricsExporter exporter, int snapshotIntervalSeconds) {
        Objects.requireNonNull(exporter, "exporter must not be null");
        return new DefaultMetricsSnapshotManager(
                Executors::newSingleThreadScheduledExecutor, snapshotIntervalSeconds, List.of(), List.of(exporter));
    }

    public static MetricsSnapshotManager createSnapshotManager(@NonNull PullingMetricsExporter exporter) {
        return new SinglePullingExporterMetricsSnapshotManager(exporter);
    }
}
