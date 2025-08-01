// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cache.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.utils.Unit;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;
import org.hiero.metrics.demo.crawler.cache.guava.config.CacheConfig;

public class GuavaDocumentCache implements DocumentCache {

    private static final Logger logger = LogManager.getLogger(GuavaDocumentCache.class);

    private final Cache<URI, Optional<Document>> cache;
    private final AtomicBoolean metricsRegistered = new AtomicBoolean(false);

    public GuavaDocumentCache(String cacheSpec) {
        cache = CacheBuilder.from(cacheSpec).recordStats().build();
    }

    public GuavaDocumentCache(CacheConfig cacheConfig) {
        this(cacheConfig.spec());
    }

    @Override
    public void registerMetrics(@NonNull MetricRegistry metricRegistry) {
        Objects.requireNonNull(metricRegistry, "metrics registry must not be null");

        // safety from calling this multiple times
        if (metricsRegistered.compareAndSet(false, true)) {
            final String cacheCategory = "cache_crawler";

            metricRegistry.register(CallbackMetric.builder(
                            CallbackMetric.key(cacheCategory, "size"), callback -> callback.call(cache.size()))
                    .withDescription("Crawl results cache size"));

            metricRegistry.register(
                    CallbackMetric.builder(CallbackMetric.key(cacheCategory, "lookups_count"), callback -> {
                                callback.call(cache.stats().hitCount(), "hit");
                                callback.call(cache.stats().missCount(), "miss");
                            })
                            .withDynamicLabelNames("type")
                            .withDescription("Crawl cache lookups count (miss or hit)"));

            metricRegistry.register(
                    CallbackMetric.builder(CallbackMetric.key(cacheCategory, "loads_count"), callback -> {
                                callback.call(cache.stats().loadSuccessCount(), "success");
                                callback.call(cache.stats().loadExceptionCount(), "exception");
                            })
                            .withDynamicLabelNames("type")
                            .withDescription("Crawl cache loads count (success or exception)"));

            metricRegistry.register(CallbackMetric.builder(
                            CallbackMetric.key(cacheCategory, "eviction_count"),
                            callback -> callback.call(cache.stats().evictionCount()))
                    .withDescription("Crawl cache eviction count"));

            metricRegistry.register(CallbackMetric.builder(
                            CallbackMetric.key(cacheCategory, "avg_load_time"),
                            callback -> callback.call(cache.stats().averageLoadPenalty()))
                    .withUnit(Unit.NANOSECOND_UNIT)
                    .withDescription("Crawl cache average load time in nanoseconds (successful and failed loads)"));
        } else {
            logger.debug("Cache metrics already registered");
        }
    }

    @Override
    @NonNull
    public Optional<Document> fetchIfAbsent(URI uri, DocumentFetcher fetcher) throws DocumentFetchException {
        try {
            return cache.get(uri, () -> fetcher.fetch(uri));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof DocumentFetchException) {
                // rethrow the original exception
                throw (DocumentFetchException) e.getCause();
            }
            throw new DocumentFetchException(e);
        }
    }
}
