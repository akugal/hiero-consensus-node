// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.core.IdempotentMetricRegistryAware;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.job.JobConfig;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.JobScheduler;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

public class DefaultJobManager extends IdempotentMetricRegistryAware implements JobManager {

    private static final Logger logger = LogManager.getLogger(DefaultJobManager.class);

    private final JobScheduler jobScheduler;
    private final Map<Integer, ScheduledJob> activeJobs = new ConcurrentHashMap<>();
    private final List<SchemeCrawler> schemeCrawlers = new CopyOnWriteArrayList<>();

    public DefaultJobManager(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @Override
    protected void registerMetricsNonIdempotent(@NonNull MetricRegistry registry) {
        jobScheduler.registerMetrics(registry);
    }

    @Override
    public void shootdown() {
        jobScheduler.shutdown();
    }

    @Override
    public void registerScheme(SchemeCrawler schemeCrawler) {
        if (schemeCrawler == null || schemeCrawler.processors().isEmpty()) {
            throw new IllegalArgumentException("Scheme processor and its processors must not be null or empty");
        }
        logger.info(
                "Registering scheme fetcher '{}' with processors: {}",
                schemeCrawler.getName(),
                schemeCrawler.processors().keySet());
        schemeCrawlers.add(schemeCrawler);
    }

    @Override
    public List<SchemeCrawler> schemes() {
        return Collections.unmodifiableList(schemeCrawlers);
    }

    @Override
    public ScheduledJob schedule(String uriStr, Duration timeout, int depth, String... processors) throws JobException {
        if (depth < 0 || depth > 50) {
            throw new JobException("Depth must be between 0 and 50: " + depth);
        }

        final URI uri;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            throw new JobException("Invalid URI: " + uriStr, e);
        }

        for (SchemeCrawler schemeCrawler : schemeCrawlers) {
            if (schemeCrawler.supports(uri)) {
                final Collection<DocumentProcessor> processorsToUse;

                if (processors == null || processors.length == 0) {
                    processorsToUse = schemeCrawler.processors().values();
                } else {
                    processorsToUse = new ArrayList<>(processors.length);
                    for (String processor : processors) {
                        DocumentProcessor docProcessor =
                                schemeCrawler.processors().get(processor);
                        if (docProcessor == null) {
                            throw new JobException("Unknown processor '" + processor + "' for '"
                                    + schemeCrawler.getName() + "'." + " Available processors: "
                                    + schemeCrawler.processors().keySet());
                        }
                        processorsToUse.add(docProcessor);
                    }
                }

                JobConfig config = new JobConfig(schemeCrawler.fetcher(), processorsToUse, depth, timeout);
                ScheduledJob scheduledJob = jobScheduler.schedule(uri, config);

                activeJobs.put(scheduledJob.getJobId(), scheduledJob);
                return scheduledJob;
            }
        }

        throw new JobException("No scheme processor found for URI: " + uriStr);
    }

    @Override
    public Optional<ScheduledJob> getJob(int jobId) {
        return Optional.ofNullable(activeJobs.get(jobId));
    }
}
