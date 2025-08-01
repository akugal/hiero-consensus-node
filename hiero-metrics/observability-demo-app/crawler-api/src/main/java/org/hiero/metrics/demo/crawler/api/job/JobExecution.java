// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.net.URI;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public final class JobExecution {

    private final URI rootUri;
    private final TypedMap context = TypedMap.createThreadSafe();
    private final JobMetrics.Builder metrics = JobMetrics.builder();

    public JobExecution(URI rootUri) {
        this.rootUri = rootUri;
    }

    public JobResult buildResult() {
        return new JobResult(rootUri(), context(), metrics.build());
    }

    public URI rootUri() {
        return rootUri;
    }

    public TypedMap context() {
        return context;
    }

    public JobMetrics.Builder metrics() {
        return metrics;
    }
}
