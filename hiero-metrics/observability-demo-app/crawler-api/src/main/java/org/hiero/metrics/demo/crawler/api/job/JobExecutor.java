// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.net.URI;
import org.hiero.metrics.api.core.MetricsBinder;
import org.hiero.metrics.demo.crawler.api.exception.JobException;

public interface JobExecutor extends MetricsBinder {

    JobResult execute(URI uri, JobConfig config) throws JobException;

    void shutdown();
}
