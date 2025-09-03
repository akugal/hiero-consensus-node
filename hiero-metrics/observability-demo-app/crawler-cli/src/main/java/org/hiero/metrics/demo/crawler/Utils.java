// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler;

import com.swirlds.config.api.Configuration;
import com.swirlds.config.api.ConfigurationBuilder;
import com.swirlds.config.extensions.sources.ClasspathFileConfigSource;
import com.swirlds.config.extensions.sources.SystemPropertiesConfigSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;
import org.hiero.metrics.api.export.MetricsExportManager;
import org.hiero.metrics.demo.crawler.api.job.JobManager;

public final class Utils {

    private Utils() {}

    public static JobManager initializeJobManager(String testName) {
        Configuration configuration;
        try {
            configuration = ConfigurationBuilder.create()
                    .autoDiscoverExtensions()
                    .withSources(new ClasspathFileConfigSource(Path.of("application.properties")))
                    .withSources(SystemPropertiesConfigSource.getInstance())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }

        MetricRegistry registry = MetricsFacade.createRegistryWithDiscoveredProviders(new Label("test", testName));
        MetricsExportManager exportManager = MetricsFacade.createExportManagerWithDiscoveredExporters(
                "crawler", configuration, Executors::newSingleThreadScheduledExecutor, 1);
        exportManager.manageMetricRegistry(registry);

        JobManager jobManager = JobManager.create(configuration);
        jobManager.bind(registry);

        return jobManager;
    }
}
