// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.demo.crawler.http {
    requires transitive org.hiero.metrics.demo.crawler.api;
    requires transitive org.apache.logging.log4j;
    requires transitive org.jsoup;
    requires java.net.http;

    provides org.hiero.metrics.demo.crawler.api.document.SchemeCrawler with
            org.hiero.metrics.demo.crawler.http.HttpSchemeCrawler;
}
