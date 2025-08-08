// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hiero.metrics.api.core.IdempotentMetricRegistryAware;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricRegistryAware;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.job.JobConfig;
import org.hiero.metrics.demo.crawler.api.job.JobScheduler;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

public class ExecutorServiceJobScheduler extends IdempotentMetricRegistryAware implements JobScheduler {

    private static final Logger logger = LogManager.getLogger(ExecutorServiceJobScheduler.class);

    private static final String JOB_ID_KEY = "job_id";

    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private final ExecutorService executorService;
    private final DocumentCache cache;

    public ExecutorServiceJobScheduler(ExecutorService executorService, DocumentCache cache) {
        this.executorService = executorService;
        this.cache = cache;
    }

    @Override
    protected void registerMetricsNonIdempotent(@NonNull MetricRegistry registry) {
        cache.registerMetrics(registry);
        if (executorService instanceof MetricRegistryAware registryAware) {
            registryAware.registerMetrics(registry);
        }
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public boolean awaitTermination(Duration timeout) throws InterruptedException {
        return executorService.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledJob schedule(URI uri, JobConfig config) {
        final int jobId = idGenerator.getAndIncrement();
        ThreadContext.put(JOB_ID_KEY, String.valueOf(jobId));
        logger.info("Job submitted. uri={}", uri);

        try {
            final JobExecution execution = new JobExecution(uri);
            final JobConfig jobConfig = new JobConfig(
                    wrapTrackingMetrics(config.fetcher(), execution),
                    config.processors(),
                    config.depth(),
                    config.timeout());

            return new FutureScheduledJob(
                    jobId,
                    executorService.submit(() -> {
                        logger.info("Job started. uri={}", uri);
                        execution.jobStarted();

                        execution.processingContext().encounterUri(uri);

                        try {
                            executeRecursive(uri, execution, jobConfig, jobConfig.depth());
                            execution.concurrencyContext().waitForTasksToComplete(jobConfig.timeout());
                            return execution.buildResult();
                        } finally {
                            logger.info("Job finished. uri={}", uri);
                        }
                    }),
                    execution.concurrencyContext());
        } finally {
            ThreadContext.remove(JOB_ID_KEY);
        }
    }

    private DocumentFetcher wrapTrackingMetrics(DocumentFetcher fetcher, JobExecution execution) {
        return uri -> {
            long startTime = execution.processingContext().currentTime();
            Optional<Document> doc;

            try {
                doc = fetcher.fetch(uri);
            } catch (Exception ex) {
                execution.processingContext().fetchError(startTime);
                logger.error("Fetch error. uri={}", uri, ex);
                // that will allow to cache error
                return Optional.empty();
            }

            execution.processingContext().fetchSuccess(startTime);

            if (doc.isEmpty()) {
                execution.processingContext().unsupportedUriSeen();
            }
            return doc;
        };
    }

    private Optional<Document> fetch(URI uri, DocumentFetcher fetcher) {
        try {
            if (cache != null) {
                return cache.fetchIfAbsent(uri, fetcher);
            } else {
                return fetcher.fetch(uri);
            }
        } catch (Throwable ex) {
            // should not happen due to wrapped fetcher, but just in case
            throw new RuntimeException("Unexpected error while fetching document. uri=" + uri, ex);
        }
    }

    private void executeRecursive(URI uri, JobExecution execution, JobConfig config, int depth) {
        if (execution.concurrencyContext().isCancelled()
                || Thread.currentThread().isInterrupted()) {
            return;
        }

        Optional<Document> optionalDocument = fetch(uri, config.fetcher());
        if (optionalDocument.isEmpty()) {
            return;
        }

        final Document document = optionalDocument.get();
        final List<URI> links = document.getLinks();

        if (depth > 0 && !links.isEmpty()) {
            // final Map<String, String> threadContext = ThreadContext.getContext();

            for (URI link : links) {
                if (execution.processingContext().encounterUri(link)) {
                    try {
                        final long submitTime = execution.concurrencyContext().currentTime();
                        execution.concurrencyContext().taskSubmitted(executorService.submit(() -> {
                            long startTimeNano = execution.concurrencyContext().taskStarted(submitTime);
                            try {
                                executeRecursive(link, execution, config, depth - 1);
                            } finally {
                                execution.concurrencyContext().taskEnded(startTimeNano);
                            }
                        }));
                    } catch (RejectedExecutionException ex) {
                        execution.concurrencyContext().taskRejected();
                    }
                } else {
                    logger.debug("Duplicate URI. uri={}", link);
                }
            }
        }

        safeProcess(document, execution, config.processors());
    }

    private void safeProcess(Document document, JobExecution execution, Collection<DocumentProcessor> processors) {
        long startTime = execution.processingContext().currentTime();
        for (DocumentProcessor processor : processors) {
            try {
                processor.process(document, execution.data());
            } catch (Throwable ex) {
                execution.processingContext().processError();
                logger.error("Process failed. processor={}, uri={}", processor.getName(), document.getUri(), ex);
            }
        }
        execution.processingContext().processFinished(startTime);
    }
}
