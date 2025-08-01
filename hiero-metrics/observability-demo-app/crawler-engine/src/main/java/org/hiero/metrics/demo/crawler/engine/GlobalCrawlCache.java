// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.engine;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.utils.Unit;

public class GlobalCrawlCache {

    private static final Logger logger = LogManager.getLogger(GlobalCrawlCache.class);

    private final Cache<URI, CrawlResult> cache;
    private final AtomicBoolean metricsRegistered = new AtomicBoolean(false);

    public GlobalCrawlCache(int maxSize) {
        cache = CacheBuilder.newBuilder().maximumSize(maxSize).recordStats().build();
    }

    public void registerCacheMetrics(@NonNull MetricRegistry metricRegistry) {
        Objects.requireNonNull(metricRegistry, "metrics registry must not be null");

        // safety from calling this multiple times
        if (metricsRegistered.compareAndSet(false, true)) {
            final String cacheCategory = "crawler_cache";

            metricRegistry.register(CallbackMetric.builder(
                            CallbackMetric.key(cacheCategory, "size"), callback -> callback.call(cache.size()))
                    .withDescription("Crawl results cache size"));

            metricRegistry.register(CallbackMetric.builder(CallbackMetric.key(cacheCategory, "lookups_count"), callback -> {
                        callback.call(cache.stats().hitCount(), "hit");
                        callback.call(cache.stats().missCount(), "miss");
                    })
                    .withDynamicLabelNames("type")
                    .withDescription("Crawl cache lookups count (miss or hit)"));

            metricRegistry.register(CallbackMetric.builder(CallbackMetric.key(cacheCategory, "loads_count"), callback -> {
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
            logger.warn("Cache metrics already registered");
        }
    }

    @NonNull
    public CrawlResult computeIfAbsent(CrawlingUrlCallable loader) throws ExecutionException {
        return cache.get(loader.getUrl(), loader);
    }
}
