package org.hiero.metrics.demo.crawler;

import com.swirlds.config.api.Configuration;
import com.swirlds.config.api.ConfigurationBuilder;
import com.swirlds.config.extensions.sources.ClasspathFileConfigSource;
import com.swirlds.config.extensions.sources.SystemPropertiesConfigSource;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;
import org.hiero.metrics.api.export.MetricsExportManager;
import org.hiero.metrics.demo.crawler.api.job.JobManager;

import java.io.IOException;
import java.nio.file.Path;

public final class Utils {

    private Utils() {}

    public static JobManager initializeJobManager(String testName) {
        MetricRegistry registry = MetricsFacade.createRegistryWithDiscoveredProviders(new Label("test", testName));
        MetricsExportManager exportManager = MetricsFacade.getDefaultExportManager();
        exportManager.manageMetricRegistry(registry);

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

        JobManager jobManager = JobManager.create(configuration);
        jobManager.registerMetrics(registry);

        return jobManager;
    }
}
