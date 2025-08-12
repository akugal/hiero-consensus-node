// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.time.Duration;
import java.util.Optional;
import org.hiero.metrics.demo.crawler.api.exception.JobException;

public interface ScheduledJob {

    int getJobId();

    boolean isDone();

    boolean isCancelled();

    default Optional<JobResult> getIfDone() {
        if (!isDone()) {
            return Optional.empty();
        }
        try {
            return Optional.of(getResult());
        } catch (JobException e) {
            return Optional.empty();
        }
    }

    JobResult getResult(Duration timeout) throws JobException;

    JobResult getResult() throws JobException;

    /**
     * Cancels the job if it is still running.
     */
    void cancel();
}
