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
import org.hiero.metrics.api.export.MetricsExportManager;
import org.hiero.metrics.api.export.PullingMetricsExporter;
import org.hiero.metrics.api.export.PushingMetricsExporter;
import org.hiero.metrics.api.utils.MetricUtils;
import org.hiero.metrics.internal.core.DefaultMetricRegistry;
import org.hiero.metrics.internal.export.DefaultMetricsExportManager;
import org.hiero.metrics.internal.export.NoOpMetricsExportManager;
import org.hiero.metrics.internal.export.SinglePullingExporterMetricsExportManager;

public final class MetricsFacade {

    private static final Logger logger = LogManager.getLogger(MetricsFacade.class);

    private static final class DefaultExportManagerHolder {
        private static final MetricsExportManager INSTANCE =
                createExportManagerWithDiscoveredExporters("default", Executors::newSingleThreadScheduledExecutor, 1);
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

        return registry;
    }

    public static MetricsExportManager getDefaultExportManager() {
        return DefaultExportManagerHolder.INSTANCE;
    }

    public static MetricsExportManager createExportManagerWithDiscoveredExporters(
            String name,
            @NonNull Supplier<ScheduledExecutorService> executorServiceFactory,
            int exportIntervalSeconds) {
        List<PullingMetricsExporter> pullingExporters = load(PullingMetricsExporter.class);
        List<PushingMetricsExporter> pushingExporters = load(PushingMetricsExporter.class);

        if (pullingExporters.isEmpty() && pushingExporters.isEmpty()) {
            logger.info("No metrics exporters found. Using no-op export manager.");
            return NoOpMetricsExportManager.INSTANCE;
        }

        if (pushingExporters.isEmpty() && pullingExporters.size() == 1) {
            logger.info("Single pulling exporter found. No export thread will be running.");
            return new SinglePullingExporterMetricsExportManager(name, pullingExporters.getFirst());
        }

        return new DefaultMetricsExportManager(
                name, executorServiceFactory, exportIntervalSeconds, pullingExporters, pushingExporters);
    }

    public static MetricsExportManager createExportManager(
            @NonNull PushingMetricsExporter exporter, int exportIntervalSeconds) {
        Objects.requireNonNull(exporter, "exporter must not be null");
        return new DefaultMetricsExportManager(
                exporter.getName(),
                Executors::newSingleThreadScheduledExecutor,
                exportIntervalSeconds,
                List.of(),
                List.of(exporter));
    }

    public static MetricsExportManager createExportManager(@NonNull PullingMetricsExporter exporter) {
        return new SinglePullingExporterMetricsExportManager(exporter.getName(), exporter);
    }
}
