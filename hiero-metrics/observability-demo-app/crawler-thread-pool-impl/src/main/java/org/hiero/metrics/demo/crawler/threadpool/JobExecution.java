// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.net.URI;
import java.time.Duration;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobMetrics;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

final class JobExecution {

    private final URI rootUri;
    private final TypedMap data = TypedMap.createThreadSafe();

    private final JobConcurrencyContext concurrencyContext;
    private final JobProcessingContext processingContext;

    public JobExecution(URI rootUri, Duration timeout, Logger logger) {
        this.rootUri = rootUri;
        processingContext = new JobProcessingContext();
        concurrencyContext = new JobConcurrencyContext(timeout, logger);
    }

    public JobResult buildResult() {
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
