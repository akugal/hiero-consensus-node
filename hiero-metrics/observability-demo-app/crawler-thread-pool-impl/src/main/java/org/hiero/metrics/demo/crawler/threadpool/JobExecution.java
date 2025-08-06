// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.net.URI;
import org.hiero.metrics.demo.crawler.api.job.JobMetrics;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

final class JobExecution {

    private final URI rootUri;
    private final TypedMap data = TypedMap.createThreadSafe();

    private final JobConcurrencyContext concurrencyContext;

    private JobProcessingContext processingContext;

    public JobExecution(URI rootUri) {
        this.rootUri = rootUri;
        concurrencyContext = new JobConcurrencyContext();
    }

    public void jobStarted() {
        long jobStartTime = concurrencyContext.jobStarted();
        processingContext = new JobProcessingContext(jobStartTime);
    }

    public JobResult buildResult() {
        if (processingContext == null) {
            throw new IllegalStateException("Job has not been started yet.");
        }
        return new JobResult(
                rootUri(), data(), new JobMetrics(processingContext.buildMetrics(), concurrencyContext.buildMetrics()));
    }

    public JobConcurrencyContext concurrencyContext() {
        return concurrencyContext;
    }

    public JobProcessingContext processingContext() {
        return processingContext;
    }

    public URI rootUri() {
        return rootUri;
    }

    public TypedMap data() {
        return data;
    }
}
