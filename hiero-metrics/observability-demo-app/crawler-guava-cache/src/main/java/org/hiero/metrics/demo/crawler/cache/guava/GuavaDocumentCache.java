// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cache.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.LongGauge;
import org.hiero.metrics.api.core.IdempotentMetricRegistryAware;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.datapoint.LongGaugeDataPoint;
import org.hiero.metrics.api.utils.Unit;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;
import org.hiero.metrics.demo.crawler.cache.guava.config.CacheConfig;

public class GuavaDocumentCache extends IdempotentMetricRegistryAware implements DocumentCache {

    private final String name;
    private final Cache<URI, Optional<Document>> cache;

    private LongGaugeDataPoint cacheSizeSpikeMax;

    public GuavaDocumentCache(CacheConfig cacheConfig) {
        this("document", cacheConfig);
    }

    public GuavaDocumentCache(String name, CacheConfig cacheConfig) {
        this.name = name;
        cache = CacheBuilder.from(cacheConfig.spec()).recordStats().build();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void registerMetricsNonIdempotent(@NonNull MetricRegistry metricRegistry) {
        final String cacheCategory = "cache_guava_" + name;

        cacheSizeSpikeMax = metricRegistry
                .register(LongGauge.maxBuilder(LongGauge.key(cacheCategory, "size_spike_max"), true)
                        .withDescription("Documents cache size - spike max"))
                .getNotLabeled();

        metricRegistry.register(CallbackMetric.builder(CallbackMetric.key(cacheCategory, "size"))
                .withDescription("Documents cache size")
                .registerDataPoint(cache::size, Map.of()));

        metricRegistry.register(CallbackMetric.builder(CallbackMetric.key(cacheCategory, "lookups_count"))
                .withDynamicLabelNames("type")
                .withDescription("Document cache lookups count (miss or hit)")
                .registerDataPoint(cache.stats()::hitCount, Map.of("type", "hit"))
                .registerDataPoint(cache.stats()::missCount, Map.of("type", "miss")));

        metricRegistry.register(CallbackMetric.builder(CallbackMetric.key(cacheCategory, "loads_count"))
                .withDynamicLabelNames("type")
                .withDescription("Document cache loads count (success or exception)")
                .registerDataPoint(cache.stats()::loadSuccessCount, Map.of("type", "success"))
                .registerDataPoint(cache.stats()::loadExceptionCount, Map.of("type", "exception")));

        metricRegistry.register(CallbackMetric.builder(CallbackMetric.key(cacheCategory, "eviction_count"))
                .withDescription("Document cache eviction count")
                .registerDataPoint(cache.stats()::evictionCount, Map.of()));

        metricRegistry.register(CallbackMetric.builder(CallbackMetric.key(cacheCategory, "avg_load_time"))
                .withUnit(Unit.NANOSECOND_UNIT)
                .withDescription("Document cache average load time in nanoseconds (successful and failed loads)")
                .registerDataPoint(cache.stats()::averageLoadPenalty, Map.of()));
    }

    @Override
    @NonNull
    public Optional<Document> fetchIfAbsent(URI uri, DocumentFetcher fetcher) throws DocumentFetchException {
        if (cacheSizeSpikeMax != null) {
            cacheSizeSpikeMax.update(cache.size());
        }

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
