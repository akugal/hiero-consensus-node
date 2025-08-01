// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.DocumentProcessor;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.job.JobConfig;
import org.hiero.metrics.demo.crawler.api.job.JobExecution;
import org.hiero.metrics.demo.crawler.api.job.JobMetrics;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.JobScheduler;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

public class ExecutorServiceJobScheduler implements JobScheduler {

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
    public void registerMetrics(MetricRegistry registry) {
        cache.registerMetrics(registry);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public ScheduledJob schedule(URI uri, JobConfig config) {
        final int jobId = idGenerator.getAndIncrement();

        return new ScheduledJob(jobId, executorService.submit(() -> {
            ThreadContext.put(JOB_ID_KEY, String.valueOf(jobId));
            try {
                return execute(uri, config);
            } finally {
                ThreadContext.remove(JOB_ID_KEY);
            }
        }));
    }

    private JobResult execute(URI uri, JobConfig jobConfig) {
        logger.info("Job start. uri={}", uri);

        final JobExecution execution = new JobExecution(uri);
        final JobConfig wrappedConfig = new JobConfig(
                wrapTrackingMetrics(jobConfig.fetcher(), execution.metrics()),
                jobConfig.processors(),
                jobConfig.depth());
        executeRecursive(uri, execution, wrappedConfig, wrappedConfig.depth());

        logger.info("Job finish. uri={}", uri);
        return execution.buildResult();
    }

    private DocumentFetcher wrapTrackingMetrics(DocumentFetcher fetcher, JobMetrics.Builder metrics) {
        return uri -> {
            long startTime = System.currentTimeMillis();
            Optional<Document> doc;

            try {
                doc = fetcher.fetch(uri);
            } catch (Exception e) {
                metrics.fetchError(System.currentTimeMillis() - startTime);
                logger.error("Fetch error. uri={}", uri, e);
                // that will allow to cache error
                return Optional.empty();
            }

            metrics.fetchSuccess(System.currentTimeMillis() - startTime);

            if (doc.isEmpty()) {
                metrics.unsupportedUriSeen();
            }
            return doc;
        };
    }

    private void executeRecursive(URI uri, JobExecution execution, JobConfig config, int depth) {
        if (depth < 0) {
            logger.warn("Depth is negative. uri={}, depth={}", uri, depth);
            return;
        }

        if (!execution.metrics().encounterUri(uri)) {
            logger.debug("Duplicate URI. uri={}", uri);
            return;
        }

        Optional<Document> optionalDocument;
        try {
            if (cache != null) {
                optionalDocument = cache.fetchIfAbsent(uri, config.fetcher());
            } else {
                optionalDocument = config.fetcher().fetch(uri);
            }
        } catch (Throwable ex) {
            // should not happen, but just in case
            throw new RuntimeException("Unexpected error while fetching document. uri=" + uri, ex);
        }

        if (optionalDocument.isEmpty()) {
            return;
        }

        final Document document = optionalDocument.get();
        final List<URI> links = document.getLinks();

        if (depth > 0 && !links.isEmpty()) {
            final CountDownLatch latchFooNestedLinks = new CountDownLatch(links.size());
            final Map<String, String> threadContext = ThreadContext.getContext();

            for (URI link : links) {
                executorService.submit(() -> {
                    ThreadContext.putAll(threadContext);
                    try {
                        executeRecursive(link, execution, config, depth - 1);
                    } finally {
                        ThreadContext.removeAll(threadContext.keySet());
                        latchFooNestedLinks.countDown();
                    }
                });
            }

            safeProcess(document, execution, config.processors());

            try {
                latchFooNestedLinks.await();
                // TODO metrics for waiting ?
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }
        } else {
            safeProcess(document, execution, config.processors());
        }
    }

    private void safeProcess(Document document, JobExecution execution, Collection<DocumentProcessor> processors) {
        long startTime = System.currentTimeMillis();
        for (DocumentProcessor processor : processors) {
            try {
                processor.process(document, execution.context());
            } catch (Throwable ex) {
                execution.metrics().processError();
                logger.error("Process failed. processor={}, uri={}", processor.getName(), document.getUri(), ex);
            }
        }
        execution.metrics().processFinished(System.currentTimeMillis() - startTime);
    }
}
