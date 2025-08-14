// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler;

import java.time.Duration;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.random.TestRandomSchemeCrawler;
import org.junit.jupiter.api.Test;

public class CrawlerTests {

    @Test
    public void testWithCacheAndWithout() throws InterruptedException {
        IdempotentTimedProcessor fetcher = new IdempotentTimedProcessor(300, 300, 0.0);
        IdempotentTimedProcessor processor = new IdempotentTimedProcessor(100, 100, 0.0);
        SchemeCrawler crawler = new TestRandomSchemeCrawler(fetcher, 0.8, 10, 10, processor);

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(50, 2, Duration.ofSeconds(30)))
                .withTimeout(Duration.ofSeconds(300))
                .withRampUpSeconds(5)
                .withThroughputPerSecond(6);

        // Provide spec for Guava cache
        // This will enable the cache with a maximum size of 1000 and expire entries after 10 seconds of access
        System.setProperty("cache.doc.guava.spec", "maximumSize=5000,expireAfterAccess=10s");
        TestUtils.run("cache-enabled", config, crawler);

        // Disable cache by setting an empty spec
        System.setProperty("cache.doc.guava.spec", "");
        TestUtils.run("cache-disabled", config, crawler);
    }

    @Test
    public void testWithThreadPoolAndVirtualThreadsForTasks() throws InterruptedException {
        // disable cache for this test
        // since cache is disabled, we will do low throughput to avoid overwhelming the system
        System.setProperty("cache.doc.guava.spec", "");

        IdempotentTimedProcessor fetcher = new IdempotentTimedProcessor(300, 400, 0.1);
        IdempotentTimedProcessor processor = new IdempotentTimedProcessor(20, 50, 0.1);
        SchemeCrawler crawler = new TestRandomSchemeCrawler(fetcher, 0.0, 10, 15, processor);

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(100, 2, Duration.ofSeconds(30)))
                .withTimeout(Duration.ofSeconds(300))
                .withRampUpSeconds(3)
                .withThroughputPerSecond(2);

        // adjust thread pool for jobs
        System.setProperty("job.pool.useVirtualThreads", "false");
        System.setProperty("job.pool.coreSize", "4");
        System.setProperty("job.pool.maxSize", "64");
        System.setProperty("job.pool.keepAliveSeconds", "3");
        System.setProperty("job.pool.queueSize", "40"); // low queue size to see pool increase

        // Adjust thread pool for job tasks
        System.setProperty("job.task.pool.useVirtualThreads", "false");
        System.setProperty("job.task.pool.coreSize", "8");
        System.setProperty("job.task.pool.maxSize", "64");
        System.setProperty("job.task.pool.keepAliveSeconds", "3");
        System.setProperty("job.task.pool.queueSize", "1024");
        TestUtils.run("tasks-thread-pool", config, crawler);

        // Use virtual thread for tasks
        System.setProperty("job.task.pool.useVirtualThreads", "true");
        TestUtils.run("tasks-virtual-threads", config, crawler);
    }
}
