// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.engine;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.hiero.metrics.api.core.MetricRegistry;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class JobResult {

    private final URI rootUrl;
    private final int urlsCrawled;
    private final int urlsCacheHits;
    private final int crawlErrors;
    private final long durationNanos;
    private final Map<String, Integer> hostToCount;

    private JobResult(Builder builder) {
        rootUrl = builder.rootUrl;
        urlsCrawled = builder.urlsCrawled.get();
        urlsCacheHits = builder.urlsCacheHits.get();
        crawlErrors = builder.crawlErrors.get();

        durationNanos = System.nanoTime() - builder.startTime;
        hostToCount = Map.copyOf(builder.hostToCount);
    }

    public static Builder builder(URI rootUrl, MetricRegistry metricRegistry, GlobalCrawlCache globalCache) {
        return new Builder(rootUrl, metricRegistry, globalCache);
    }

    public URI getRootUrl() {
        return rootUrl;
    }

    public long getDurationNanos() {
        return durationNanos;
    }

    public int getUrlsCrawled() {
        return urlsCrawled;
    }

    public int getUrlsCacheHits() {
        return urlsCacheHits;
    }

    public int getCrawlErrors() {
        return crawlErrors;
    }

    public Map<String, Integer> getSeenHostsToCount() {
        return hostToCount;
    }

    public static class Builder {

        private static final BiFunction<Integer, Integer, Integer> SUM = Integer::sum;

        private final URI rootUrl;
        private final MetricRegistry metricRegistry;
        private final Map<String, Integer> hostToCount = new ConcurrentHashMap<>();

        private final AtomicInteger urlsCrawled = new AtomicInteger();
        private final AtomicInteger urlsCacheHits = new AtomicInteger();
        private final AtomicInteger crawlErrors = new AtomicInteger();

        private final GlobalCrawlCache globalCache;
        private final Cache<URI, CrawlResult> jobCache = CacheBuilder.newBuilder().build();

        private final long startTime;

        public Builder(URI rootUrl, MetricRegistry metricRegistry, GlobalCrawlCache globalCache) {
            this.rootUrl = rootUrl;
            this.metricRegistry = metricRegistry;
            this.globalCache = globalCache;

            startTime = System.nanoTime();
        }

        public Optional<CrawlResult> crawl(URI url) {
            CrawlingUrlCallable crawler = new CrawlingUrlCallable(url, metricRegistry);
            TrackingCallable<CrawlResult> globalLoader = new TrackingCallable<>(() -> globalCache.computeIfAbsent(crawler));
            TrackingCallable<CrawlResult> jobLoader = new TrackingCallable<>(globalLoader);

            CrawlResult result;
            try {
                result = jobCache.get(url, jobLoader);
            } catch (Exception ex) {
                crawlErrors.incrementAndGet();
                // nothing else to do with exception - it should be logged inside the callable
                return Optional.empty();
            }

            if (jobLoader.isExecuted()) {
                if (globalLoader.isExecuted()) {
                    urlsCrawled.incrementAndGet();
                } else {
                    urlsCacheHits.incrementAndGet();
                }
                observeHosts(result);

                return Optional.of(result);
            } else {
                // we do not process same url withing the job twice
                return Optional.empty();
            }
        }

        private void observeHosts(CrawlResult result) {
            for (URI reference : result.references()) {
                String host = reference.getHost();
                if (host != null) {
                    if (host.startsWith("www.")) {
                        host = host.substring(4); // Normalize by removing 'www.'
                    }

                    hostToCount.merge(host, 1, SUM);
                }
            }
        }

        public JobResult build() {
            return new JobResult(this);
        }
    }
}
