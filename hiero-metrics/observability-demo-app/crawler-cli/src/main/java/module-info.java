// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.demo.crawler.cli {
    exports org.hiero.metrics.demo.crawler;
    exports org.hiero.metrics.demo.crawler.cli;

    uses org.hiero.metrics.demo.crawler.api.job.JobSchedulerFactory;

    requires com.swirlds.config.api;
    requires com.swirlds.config.extensions;
    requires org.hiero.metrics.core;
    requires org.hiero.metrics.demo.crawler.api;
    requires org.apache.logging.log4j;

    provides org.hiero.metrics.demo.crawler.api.document.SchemeCrawler with
            org.hiero.metrics.demo.crawler.file.FileSchemeCrawler;
    provides org.hiero.metrics.api.export.PushingMetricsExporter with
            org.hiero.metrics.demo.crawler.DemoCsvMetricsFileExporter;
}
