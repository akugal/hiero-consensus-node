// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.util.Optional;
import org.hiero.metrics.demo.crawler.api.exception.JobException;

public interface ScheduledJob {

    int getJobId();

    boolean isDone();

    boolean isCancelled();

    Optional<JobResult> tryGetResult();

    JobResult getResult() throws JobException;

    /**
     * Cancels the job if it is still running.
     */
    void cancel();
}
