// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.core.IdempotentMetricRegistryAware;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricRegistryAware;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.job.JobConfig;
import org.hiero.metrics.demo.crawler.api.job.JobExecutor;
import org.hiero.metrics.demo.crawler.api.job.JobResult;

public class ExecutorServiceJobExecutor extends IdempotentMetricRegistryAware implements JobExecutor {

    private static final Logger logger = LogManager.getLogger(ExecutorServiceJobExecutor.class);

    private final ExecutorService executorService;
    private final DocumentCache cache;

    public ExecutorServiceJobExecutor(ExecutorService executorService, DocumentCache cache) {
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
    public JobResult execute(URI uri, JobConfig config) throws JobException {
        logger.info("Job started. uri={}", uri);

        final JobExecution execution = new JobExecution(uri, config.timeout(), logger);
        final JobConfig jobConfig = new JobConfig(
                wrapTrackingMetrics(config.fetcher(), execution),
                config.processors(),
                config.depth(),
                config.timeout());

        execution.processingContext().encounterUri(uri);

        Future<?> rootFuture = executorService.submit(new JobTask(uri, execution, jobConfig, jobConfig.depth()));
        execution.concurrencyContext().taskSubmitted(rootFuture);

        logger.debug("Waiting for tasks to complete. uri={}", uri);
        execution.concurrencyContext().waitForTasksToComplete();

        logger.info("Job finished. uri={}", uri);
        return execution.buildResult();
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

    private class JobTask implements Runnable {

        protected final URI uri;
        protected final JobExecution execution;
        private final JobConfig config;
        private final int depth;
        private final long submitTime;

        public JobTask(URI uri, JobExecution execution, JobConfig config, int depth) {
            this.uri = uri;
            this.execution = execution;
            this.config = config;
            this.depth = depth;
            submitTime = execution.concurrencyContext().currentTime();
        }

        @Override
        public void run() {
            if (execution.concurrencyContext().isCancelled()) {
                logger.debug("Job cancelled. Skipping task. uri={}", uri);
                return;
            }
            if (Thread.currentThread().isInterrupted()) {
                logger.debug("Job interrupted. Skipping task. uri={}", uri);
                return;
            }

            long startTimeNano = execution.concurrencyContext().taskStarted(submitTime);
            try {
                crawl();
            } finally {
                execution.concurrencyContext().taskEnded(startTimeNano);
            }
        }

        private void crawl() {
            Optional<Document> optionalDocument = fetch();
            if (optionalDocument.isEmpty()) {
                return;
            }

            final Document document = optionalDocument.get();
            final List<URI> links = document.getLinks();

            if (depth > 0 && !links.isEmpty()) {
                logger.debug("Discovered {} nested links. uri={}", links.size(), uri);

                for (URI link : links) {
                    if (execution.processingContext().encounterUri(link)) {
                        try {
                            Future<?> submit = executorService.submit(new JobTask(link, execution, config, depth - 1));
                            execution.concurrencyContext().taskSubmitted(submit);
                        } catch (RejectedExecutionException ex) {
                            execution.concurrencyContext().taskRejected();
                        }
                    } else {
                        logger.debug("Duplicate URI. uri={}", link);
                    }
                }
            }

            safeProcess(document);
        }

        private Optional<Document> fetch() {
            try {
                if (cache != null) {
                    return cache.fetchIfAbsent(uri, config.fetcher());
                } else {
                    return config.fetcher().fetch(uri);
                }
            } catch (Throwable ex) {
                // should not happen due to wrapped fetcher, but just in case
                throw new RuntimeException("Unexpected error while fetching document. uri=" + uri, ex);
            }
        }

        private void safeProcess(Document document) {
            long startTime = execution.processingContext().currentTime();
            for (DocumentProcessor processor : config.processors()) {
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
}
