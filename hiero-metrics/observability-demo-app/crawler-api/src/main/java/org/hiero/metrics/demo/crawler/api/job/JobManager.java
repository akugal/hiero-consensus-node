// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import com.swirlds.config.api.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.hiero.metrics.api.core.MetricRegistryAware;
import org.hiero.metrics.demo.crawler.api.document.SchemeProcessor;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.internal.DefaultJobManager;

public interface JobManager extends MetricRegistryAware {

    static JobManager create(Configuration configuration) {
        JobSchedulerFactory jobSchedulerFactory =
                ServiceLoader.load(JobSchedulerFactory.class).findFirst().orElseThrow();

        JobScheduler jobScheduler = jobSchedulerFactory.createJobScheduler(configuration);
        JobManager jobManager = new DefaultJobManager(jobScheduler);

        ServiceLoader<SchemeProcessor> loader = ServiceLoader.load(SchemeProcessor.class);
        for (SchemeProcessor schemeProcessor : loader) {
            jobManager.registerScheme(schemeProcessor);
        }
        return jobManager;
    }

    void registerScheme(SchemeProcessor schemeProcessor);

    List<SchemeProcessor> schemes();

    ScheduledJob schedule(String uri, int depth, String... processors) throws JobException;

    Optional<ScheduledJob> getJob(int jobId);
}
