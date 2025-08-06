// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.demo.crawler.cache.guava {
    exports org.hiero.metrics.demo.crawler.cache.guava.config to
            com.swirlds.config.impl,
            com.swirlds.config.extensions;

    requires transitive com.swirlds.config.api;
    requires transitive org.hiero.metrics.core;
    requires transitive org.hiero.metrics.demo.crawler.api;
    requires com.google.common;
    requires org.apache.logging.log4j;

    provides org.hiero.metrics.demo.crawler.api.document.cache.DocumentCacheFactory with
            org.hiero.metrics.demo.crawler.cache.guava.GuavaDocumentCacheFactory;
    provides com.swirlds.config.api.ConfigurationExtension with
            org.hiero.metrics.demo.crawler.cache.guava.config.CacheConfigurationExtension;
}
