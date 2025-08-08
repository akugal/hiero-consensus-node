// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

final class FutureScheduledJob implements ScheduledJob {

    private final int jobId;
    private final Future<JobResult> future;
    private final JobConcurrencyContext context;

    public FutureScheduledJob(int jobId, Future<JobResult> future, JobConcurrencyContext context) {
        this.jobId = jobId;
        this.future = future;
        this.context = context;
    }

    @Override
    public int getJobId() {
        return jobId;
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public boolean isCancelled() {
        return context.isCancelled() || future.isCancelled();
    }

    @Override
    public JobResult getResult() throws JobException {
        try {
            return future.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof JobException) {
                throw (JobException) e.getCause();
            } else {
                throw new JobException("Job failed", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JobException("Job interrupted", e);
        }
    }

    @Override
    public void cancel() {
        context.cancel();
        future.cancel(true);
    }
}
