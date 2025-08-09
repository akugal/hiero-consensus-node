// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cache.guava;

import com.swirlds.config.api.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCacheFactory;
import org.hiero.metrics.demo.crawler.cache.guava.config.CacheConfig;
import org.hiero.metrics.demo.crawler.api.document.cache.NoOpDocumentCache;

public class GuavaDocumentCacheFactory implements DocumentCacheFactory {

    private static final Logger logger = LogManager.getLogger(GuavaDocumentCacheFactory.class);

    @Override
    public DocumentCache createDocumentCache(Configuration configuration) {
        CacheConfig cacheConfig = configuration.getConfigData(CacheConfig.class);
        if (cacheConfig.spec() == null || cacheConfig.spec().isBlank()) {
            logger.warn("Cache spec is null or blank - using NoOpDocumentCache");
            return NoOpDocumentCache.INSTANCE;
        }

        logger.info("Creating Guava document cache with: {}", cacheConfig);
        return new GuavaDocumentCache(cacheConfig);
    }
}
