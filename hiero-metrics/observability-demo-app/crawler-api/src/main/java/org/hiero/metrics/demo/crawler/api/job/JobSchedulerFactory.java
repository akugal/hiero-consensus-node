// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import com.swirlds.config.api.Configuration;

@FunctionalInterface
public interface JobSchedulerFactory {

    JobScheduler createJobScheduler(Configuration configuration);
}
