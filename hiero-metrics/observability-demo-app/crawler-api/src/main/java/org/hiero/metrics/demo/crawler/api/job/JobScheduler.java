// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.net.URI;
import org.hiero.metrics.api.core.MetricRegistryAware;

public interface JobScheduler extends MetricRegistryAware {

    ScheduledJob schedule(URI uri, JobConfig config);

    void shutdown();
}
