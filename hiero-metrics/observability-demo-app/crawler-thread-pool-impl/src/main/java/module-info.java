// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.demo.crawler.threadpool {
    uses org.hiero.metrics.demo.crawler.api.document.cache.DocumentCacheFactory;

    exports org.hiero.metrics.demo.crawler.threadpool.config to
            com.swirlds.config.impl,
            com.swirlds.config.extensions;

    requires transitive com.swirlds.config.api;
    requires transitive org.hiero.metrics.demo.crawler.api;
    requires org.hiero.metrics.core;
    requires org.apache.logging.log4j;

    provides org.hiero.metrics.demo.crawler.api.job.JobSchedulerFactory with
            org.hiero.metrics.demo.crawler.threadpool.ExecutorServiceJobSchedulerFactory;
    provides com.swirlds.config.api.ConfigurationExtension with
            org.hiero.metrics.demo.crawler.threadpool.config.TrheadPoolConfigurationExtension;
}
