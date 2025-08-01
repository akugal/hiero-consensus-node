// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.internal;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.SchemeProcessor;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.job.JobConfig;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.JobScheduler;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

public class DefaultJobManager implements JobManager {

    private static final Logger logger = LogManager.getLogger(DefaultJobManager.class);

    private final JobScheduler jobScheduler;
    private final Map<Integer, ScheduledJob> activeJobs = new ConcurrentHashMap<>();
    private final List<SchemeProcessor> schemeProcessors = new CopyOnWriteArrayList<>();

    public DefaultJobManager(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
    }

    @Override
    public void registerMetrics(MetricRegistry registry) {
        jobScheduler.registerMetrics(registry);
    }

    @Override
    public void registerScheme(SchemeProcessor schemeProcessor) {
        if (schemeProcessor == null || schemeProcessor.processors().isEmpty()) {
            throw new IllegalArgumentException("Scheme processor and its processors must not be null or empty");
        }
        logger.info("Registering scheme fetcher '{}' with processors: {}",
                schemeProcessor.getName(), schemeProcessor.processors().keySet());
        schemeProcessors.add(schemeProcessor);
    }

    @Override
    public List<SchemeProcessor> schemes() {
        return Collections.unmodifiableList(schemeProcessors);
    }

    @Override
    public ScheduledJob schedule(String uriStr, int depth, String... processors) throws JobException {
        if (depth < 0 || depth > 50) {
            throw new JobException("Depth must be between 0 and 50: " + depth);
        }

        final URI uri;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            throw new JobException("Invalid URI: " + uriStr, e);
        }

        for (SchemeProcessor schemeProcessor : schemeProcessors) {
            if (schemeProcessor.supports(uri)) {
                final Collection<DocumentProcessor> processorsToUse;

                if (processors == null || processors.length == 0) {
                    processorsToUse = schemeProcessor.processors().values();
                } else {
                    processorsToUse = new ArrayList<>(processors.length);
                    for (String processor : processors) {
                        DocumentProcessor docProcessor =
                                schemeProcessor.processors().get(processor);
                        if (docProcessor == null) {
                            throw new JobException("Unknown processor '" + processor + "' for '"
                                    + schemeProcessor.getName() + "'." + " Available processors: "
                                    + schemeProcessor.processors().keySet());
                        }
                        processorsToUse.add(docProcessor);
                    }
                }

                JobConfig config = new JobConfig(schemeProcessor.fetcher(), processorsToUse, depth);
                ScheduledJob scheduledJob = jobScheduler.schedule(uri, config);

                activeJobs.put(scheduledJob.jobId(), scheduledJob);
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
