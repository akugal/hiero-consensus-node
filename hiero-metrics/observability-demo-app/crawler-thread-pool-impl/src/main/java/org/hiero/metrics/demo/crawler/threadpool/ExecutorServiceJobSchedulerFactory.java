// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import com.swirlds.config.api.Configuration;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCacheFactory;
import org.hiero.metrics.demo.crawler.api.job.JobScheduler;
import org.hiero.metrics.demo.crawler.api.job.JobSchedulerFactory;
import org.hiero.metrics.demo.crawler.threadpool.config.JobPoolConfig;
import org.hiero.metrics.demo.crawler.threadpool.metrics.ExecutorServiceFactory;

public class ExecutorServiceJobSchedulerFactory implements JobSchedulerFactory {

    private static final Logger logger = LogManager.getLogger(ExecutorServiceJobSchedulerFactory.class);

    @Override
    public JobScheduler createJobScheduler(Configuration configuration) {
        DocumentCache documentCache = ServiceLoader.load(DocumentCacheFactory.class)
                .findFirst()
                .orElse(DocumentCacheFactory.NO_OP)
                .createDocumentCache(configuration);

        JobPoolConfig jobTaskPoolConfig = configuration.getConfigData(JobPoolConfig.class);
        logger.info("Creating tread pool executor with config: {}", jobTaskPoolConfig);
        ExecutorService jobExecutorService = ExecutorServiceFactory.buildExecutorService(jobTaskPoolConfig);

        return new ExecutorServiceJobScheduler(jobExecutorService, documentCache);
    }
}
