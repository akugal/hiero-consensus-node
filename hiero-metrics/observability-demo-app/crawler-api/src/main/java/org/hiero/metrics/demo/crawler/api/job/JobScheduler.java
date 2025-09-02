// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.net.URI;
import java.time.Duration;
import org.hiero.metrics.api.core.MetricsBinder;

public interface JobScheduler extends MetricsBinder {

    boolean awaitTermination(Duration timeout) throws InterruptedException;

    ScheduledJob schedule(URI uri, JobConfig config);

    void shutdown();
}
