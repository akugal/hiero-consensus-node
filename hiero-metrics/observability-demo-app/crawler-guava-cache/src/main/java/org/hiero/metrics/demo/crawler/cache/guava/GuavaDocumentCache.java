// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.cache.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.hiero.metrics.api.StatelessMetric;
import org.hiero.metrics.api.core.IdempotentMetricsBinder;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.utils.Unit;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;
import org.hiero.metrics.demo.crawler.cache.guava.config.CacheConfig;

public class GuavaDocumentCache extends IdempotentMetricsBinder implements DocumentCache {

    private final String name;
    private final Cache<URI, Optional<Document>> cache;

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
    protected void bindMetricsNonIdempotent(@NonNull MetricRegistry metricRegistry) {
        final String category = "cache_guava_" + name;

        // cache size metrics
        metricRegistry.register(
                StatelessMetric.builder(StatelessMetric.key("size").withCategory(category))
                        .withDescription("Documents cache size")
                        .registerDataPoint(cache::size, Map.of()));

        // cache lookup count
        metricRegistry.register(
                StatelessMetric.builder(StatelessMetric.key("lookups_count").withCategory(category))
                        .withDynamicLabelNames("type")
                        .withDescription("Document cache lookups count (miss or hit)")
                        .registerDataPoint(() -> cache.stats().hitCount(), Map.of("type", "hit"))
                        .registerDataPoint(() -> cache.stats().missCount(), Map.of("type", "miss")));

        // cache load count
        metricRegistry.register(
                StatelessMetric.builder(StatelessMetric.key("loads_count").withCategory(category))
                        .withDynamicLabelNames("type")
                        .withDescription("Document cache loads count (success or exception)")
                        .registerDataPoint(() -> cache.stats().loadSuccessCount(), Map.of("type", "success"))
                        .registerDataPoint(() -> cache.stats().loadExceptionCount(), Map.of("type", "exception")));

        // eviction count
        metricRegistry.register(
                StatelessMetric.builder(StatelessMetric.key("eviction_count").withCategory(category))
                        .withDescription("Document cache eviction count")
                        .registerDataPoint(() -> cache.stats().evictionCount(), Map.of()));

        // avg load time
        metricRegistry.register(StatelessMetric.builder(
                        StatelessMetric.key("avg_load_time").withCategory(category))
                .withUnit(Unit.NANOSECOND_UNIT)
                .withDescription("Document cache average load time in nanoseconds (successful and failed loads)")
                .registerDataPoint(() -> cache.stats().averageLoadPenalty(), Map.of()));
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
