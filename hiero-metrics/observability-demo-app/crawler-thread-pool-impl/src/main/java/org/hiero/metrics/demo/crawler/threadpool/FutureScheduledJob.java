// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

final class FutureScheduledJob implements ScheduledJob {

    private final int jobId;
    private final Future<JobResult> jobFuture;

    public FutureScheduledJob(int jobId, Future<JobResult> jobFuture) {
        this.jobId = jobId;
        this.jobFuture = jobFuture;
    }

    @Override
    public int getJobId() {
        return jobId;
    }

    @Override
    public boolean isDone() {
        return jobFuture.isDone();
    }

    @Override
    public boolean isCancelled() {
        return jobFuture.isCancelled();
    }

    @Override
    public Optional<JobResult> tryGetResult() {
        if (!isDone()) {
            return Optional.empty();
        }
        try {
            return Optional.of(getResult());
        } catch (JobException e) {
            return Optional.empty();
        }
    }

    @Override
    public JobResult getResult() throws JobException {
        try {
            return jobFuture.get();
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
        jobFuture.cancel(true);
    }
}
