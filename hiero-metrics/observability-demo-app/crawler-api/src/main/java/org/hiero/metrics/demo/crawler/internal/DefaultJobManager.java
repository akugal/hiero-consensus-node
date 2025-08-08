// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
    private final Map<Integer, ScheduledJob> finishedJobs = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledJob> activeJobs = new ConcurrentHashMap<>();
    private final List<SchemeCrawler> schemeCrawlers = new CopyOnWriteArrayList<>();

    private final Thread completionThread = new CompletionThread();
    private final Object jobSignal = new Object();

    private JobMetricsReporter metricsReporter;

    public DefaultJobManager(JobScheduler jobScheduler) {
        this.jobScheduler = jobScheduler;
        completionThread.start();
    }

    @Override
    protected void registerMetricsNonIdempotent(@NonNull MetricRegistry registry) {
        metricsReporter = new JobMetricsReporter(registry);
        jobScheduler.registerMetrics(registry);
    }

    @Override
    public void shutdown() {
        logger.info("Shutting down job manager and scheduler...");
        jobScheduler.shutdown();
        completionThread.interrupt();
    }

    @Override
    public boolean awaitTermination(Duration timeout) throws InterruptedException {
        return jobScheduler.awaitTermination(timeout);
    }

    @Override
    public void registerCrawler(SchemeCrawler schemeCrawler) {
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
                synchronized (jobSignal) {
                    jobSignal.notify();
                }
                return scheduledJob;
            }
        }

        throw new JobException("No scheme processor found for URI: " + uriStr);
    }

    @Override
    public Optional<ScheduledJob> getJob(int jobId) {
        ScheduledJob scheduledJob = finishedJobs.get(jobId);
        if (scheduledJob == null) {
            scheduledJob = activeJobs.get(jobId);
        }
        return Optional.ofNullable(scheduledJob);
    }

    private class CompletionThread extends Thread {

        public CompletionThread() {
            super("crawl-job-manager-completion-thread");
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (jobSignal) {
                        // Wait if no active jobs, with timeout to handle race conditions
                        while (activeJobs.isEmpty()) {
                            jobSignal.wait(1000);
                        }
                    }

                    boolean hasActiveJobs = processCompletedJobs();

                    if (hasActiveJobs) {
                        // Short sleep when jobs are still running to avoid busy waiting
                        Thread.sleep(50);
                    }
                } catch (InterruptedException e) {
                    logger.info("Completion thread interrupted, exiting...");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        private boolean processCompletedJobs() {
            Iterator<Map.Entry<Integer, ScheduledJob>> iterator =
                    activeJobs.entrySet().iterator();
            boolean hasRemainingJobs = false;

            while (iterator.hasNext()) {
                Map.Entry<Integer, ScheduledJob> entry = iterator.next();
                ScheduledJob job = entry.getValue();

                if (job.isDone()) {
                    logger.debug("Job has been done. job_id={}", job.getJobId());
                    finishedJobs.put(job.getJobId(), job);
                    iterator.remove();

                    if (metricsReporter != null) {
                        try {
                            metricsReporter.report(job.getResult());
                        } catch (JobException e) {
                            // do nothing - allow clients to handle job exceptions
                        } catch (Exception e) {
                            logger.error("Failed to report job metrics for jobId={}", job.getJobId(), e);
                        }
                    }
                } else {
                    hasRemainingJobs = true;
                }
            }

            return hasRemainingJobs;
        }
    }
}
