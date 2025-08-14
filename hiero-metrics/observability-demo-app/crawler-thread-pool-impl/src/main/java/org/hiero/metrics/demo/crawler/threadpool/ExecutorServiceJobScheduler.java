// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.hiero.metrics.api.core.IdempotentMetricRegistryAware;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricRegistryAware;
import org.hiero.metrics.demo.crawler.api.exception.JobTimeoutException;
import org.hiero.metrics.demo.crawler.api.job.JobConfig;
import org.hiero.metrics.demo.crawler.api.job.JobExecutor;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.JobScheduler;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobMetricsReporter;

public class ExecutorServiceJobScheduler extends IdempotentMetricRegistryAware implements JobScheduler {

    private static final Logger logger = LogManager.getLogger(ExecutorServiceJobScheduler.class);

    private static final String JOB_ID_KEY = "job_id";

    private final AtomicInteger idGenerator = new AtomicInteger(1);

    private final ExecutorService executorService;
    private final JobExecutor jobExecutor;
    private JobMetricsReporter metricsReporter;

    public ExecutorServiceJobScheduler(ExecutorService executorService, JobExecutor jobExecutor) {
        this.executorService = executorService;
        this.jobExecutor = jobExecutor;
    }

    @Override
    protected void registerMetricsNonIdempotent(@NonNull MetricRegistry registry) {
        jobExecutor.registerMetrics(registry);
        if (executorService instanceof MetricRegistryAware registryAware) {
            registryAware.registerMetrics(registry);
        }
        metricsReporter = new JobMetricsReporter(registry);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        jobExecutor.shutdown();
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
            if (metricsReporter != null) {
                metricsReporter.onJobSubmit(uri);
                return new FutureScheduledJob(jobId, executorService.submit(() -> {
                    try {
                        JobResult result = jobExecutor.execute(uri, config);
                        metricsReporter.onJobFinish(result);
                        return result;
                    } catch (JobTimeoutException ex) {
                        metricsReporter.onJobTimeout(uri);
                        throw ex;
                    }
                }));
            } else {
                logger.debug("Metrics are not available, executing job without metrics reporting. uri={}", uri);
                return new FutureScheduledJob(jobId, executorService.submit(() -> jobExecutor.execute(uri, config)));
            }
        } finally {
            ThreadContext.remove(JOB_ID_KEY);
        }
    }
}
