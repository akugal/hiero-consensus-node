// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.engine;

import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;

import java.net.URI;
import java.util.concurrent.ForkJoinPool;

public class CrawlEngine {

    private final GlobalCrawlCache globalCache;
    private final ForkJoinPool forkJoinPool;
    private final MetricRegistry metricRegistry;

    public CrawlEngine(GlobalCrawlCache globalCache, ForkJoinPool forkJoinPool, MetricRegistry metricRegistry) {
        this.globalCache = globalCache;
        this.forkJoinPool = forkJoinPool;
        this.metricRegistry = metricRegistry;
    }

    public JobResult crawl(String url) {
        URI uri = URI.create(url);

        JobResult.Builder resultBuilder = JobResult.builder(uri, metricRegistry, globalCache);
        forkJoinPool.submit(new CrawlingAction(uri, 2, resultBuilder)).join();
        return resultBuilder.build();
    }

    public static void main(String[] args) {
        MetricRegistry registry = MetricsFacade.createRegistry();
        GlobalCrawlCache cache = new GlobalCrawlCache(100);
        cache.registerCacheMetrics(registry);

        CrawlEngine engine = new CrawlEngine(cache, ForkJoinPool.commonPool(), registry);
        JobResult crawl = engine.crawl("https://hiero.org/");

        System.out.println("Errors:" + crawl.getCrawlErrors());
        System.out.println("Urls crawled:" + crawl.getUrlsCrawled());
        System.out.println("Cache hits crawled:" + crawl.getUrlsCacheHits());
        System.out.println(crawl.getSeenHostsToCount());
    }
}
