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
import org.hiero.metrics.api.core.IdempotentMetricRegistryAware;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.stat.CumulativeAverageIntStat;
import org.hiero.metrics.api.stat.MovingAverageStat;
import org.hiero.metrics.api.utils.Unit;
import org.hiero.metrics.demo.crawler.api.document.Document;
import org.hiero.metrics.demo.crawler.api.document.DocumentFetcher;
import org.hiero.metrics.demo.crawler.api.document.cache.DocumentCache;
import org.hiero.metrics.demo.crawler.api.exception.DocumentFetchException;
import org.hiero.metrics.demo.crawler.cache.guava.config.CacheConfig;

public class GuavaDocumentCache extends IdempotentMetricRegistryAware implements DocumentCache {

    private final String name;
    private final Cache<URI, Optional<Document>> cache;

    private CumulativeAverageIntStat cacheSizeAvg;
    private MovingAverageStat cacheSizeMovingAvg;

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
        final String category = "cache_guava_" + name;

        // cache size metrics
        metricRegistry.register(
                CallbackMetric.builder(CallbackMetric.key("size").withCategory(category))
                        .withDescription("Documents cache size")
                        .registerDataPoint(cache::size, Map.of()));
        // accumulates cache size over time and rest betwee exports; starts with current cache size
        cacheSizeAvg = metricRegistry
                .register(CumulativeAverageIntStat.metricBuilder(
                                CumulativeAverageIntStat.key("size_avg").withCategory(category))
                        .withDefaultInitializer(() -> (int) cache.size())
                        .withDescription("Documents cache size - avg cumulative"))
                .getNotLabeled();
        // moving average that also starts
        cacheSizeMovingAvg = metricRegistry
                .register(MovingAverageStat.metricBuilder(
                                1, MovingAverageStat.key("size_moving_avg").withCategory(category))
                        .withDefaultInitializer(() -> (int) cache.size())
                        .withDescription("Documents cache size - avg moving (half-life 1 sec)"))
                .getNotLabeled();

        // cache lookup count
        metricRegistry.register(
                CallbackMetric.builder(CallbackMetric.key("lookups_count").withCategory(category))
                        .withDynamicLabelNames("type")
                        .withDescription("Document cache lookups count (miss or hit)")
                        .registerDataPoint(() -> cache.stats().hitCount(), Map.of("type", "hit"))
                        .registerDataPoint(() -> cache.stats().missCount(), Map.of("type", "miss")));

        // cache load count
        metricRegistry.register(
                CallbackMetric.builder(CallbackMetric.key("loads_count").withCategory(category))
                        .withDynamicLabelNames("type")
                        .withDescription("Document cache loads count (success or exception)")
                        .registerDataPoint(() -> cache.stats().loadSuccessCount(), Map.of("type", "success"))
                        .registerDataPoint(() -> cache.stats().loadExceptionCount(), Map.of("type", "exception")));

        // eviction count
        metricRegistry.register(
                CallbackMetric.builder(CallbackMetric.key("eviction_count").withCategory(category))
                        .withDescription("Document cache eviction count")
                        .registerDataPoint(() -> cache.stats().evictionCount(), Map.of()));

        // avg load time
        metricRegistry.register(CallbackMetric.builder(
                        CallbackMetric.key("avg_load_time").withCategory(category))
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
        } finally {
            if (isMetricsRegistered()) {
                long size = cache.size();
                // we assume no config will have more than Integer.MAX_VALUE entries
                cacheSizeAvg.update((int) size);
                cacheSizeMovingAvg.update(size);
            }
        }
    }
}
