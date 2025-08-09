// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.demo.crawler.api {
    uses org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
    uses org.hiero.metrics.demo.crawler.api.job.JobSchedulerFactory;

    exports org.hiero.metrics.demo.crawler.api.document;
    exports org.hiero.metrics.demo.crawler.api.document.cache;
    exports org.hiero.metrics.demo.crawler.api.exception;
    exports org.hiero.metrics.demo.crawler.api.job;
    exports org.hiero.metrics.demo.crawler.api.util;
    exports org.hiero.metrics.demo.crawler.api.job.metrics;

    requires transitive com.swirlds.config.api;
    requires transitive org.hiero.metrics.core;
    requires org.apache.logging.log4j;

    provides org.hiero.metrics.api.core.MetricsRegistrationProvider with
            org.hiero.metrics.demo.crawler.api.job.metrics.JobMetricsRegistration;
}
