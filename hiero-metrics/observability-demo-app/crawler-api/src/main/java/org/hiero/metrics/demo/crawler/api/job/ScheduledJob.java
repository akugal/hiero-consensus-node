// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

public interface ScheduledJob {

    int getJobId();

    boolean isDone();

    boolean isCancelled();

    JobResult getResult();

    /**
     * Cancels the job if it is still running.
     */
    void cancel();
}
