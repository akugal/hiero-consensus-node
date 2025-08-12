// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler; // SPDX-License-Identifier: Apache-2.0

import java.time.Duration;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.random.TestRandomSchemeCrawler;
import org.junit.jupiter.api.Test;

public class CrawlerTests {

    @Test
    public void testWithCacheAndWithout() throws InterruptedException {
        IdempotentTimedProcessor fetcher = new IdempotentTimedProcessor(100, 200, 0.4);
        IdempotentTimedProcessor processor = new IdempotentTimedProcessor(50, 100, 0.1);
        SchemeCrawler crawler = new TestRandomSchemeCrawler(fetcher, 0.8, 5, 10, processor);

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(50, 2, Duration.ofSeconds(30)))
                .withTimeout(Duration.ofSeconds(300))
                .withThroughputPerSecond(6);

        System.out.println("Running test with cache enabled");
        // Provide spec for Guava cache
        // This will enable the cache with a maximum size of 1000 and expire entries after 20 seconds of access
        System.setProperty("cache.doc.guava.spec", "maximumSize=1000,expireAfterAccess=20s");
        TestUtils.run("cache-enabled", config, crawler);

        System.gc();
        Thread.sleep(2000); // Delay between tests to allow metrics to be collected

        System.out.println("Running test with cache disabled");
        // Disable cache by setting an empty spec
        System.setProperty("cache.doc.guava.spec", "");
        TestUtils.run("cache-disabled", config, crawler);

        Thread.sleep(2000); // allow metrics to be collected
    }

    @Test
    public void testWithThreadPoolAndVirtualThreadsForTasks() throws InterruptedException {
        // disable cache for this test
        // since cache is disabled, we will do low throughput to avoid overwhelming the system
        System.setProperty("cache.doc.guava.spec", "");

        IdempotentTimedProcessor fetcher = new IdempotentTimedProcessor(50, 100, 0.2);
        IdempotentTimedProcessor processor = new IdempotentTimedProcessor(10, 50, 0.1);
        SchemeCrawler crawler = new TestRandomSchemeCrawler(fetcher, 0.0, 10, 20, processor);

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(50, 2, Duration.ofSeconds(30)))
                .withTimeout(Duration.ofSeconds(300))
                .withThroughputPerSecond(2);

        System.out.println("---------------------------------------------");
        System.out.println("Running test with thread pool for tasks");
        System.out.println("---------------------------------------------");

        // Use thread pool for tasks
        System.setProperty("job.task.pool.useVirtualThreads", "false");
        System.setProperty("job.task.pool.coreSize", "8");
        System.setProperty("job.task.pool.maxSize", "64");
        System.setProperty("job.task.pool.queueSize", "500");
        TestUtils.run("tasks-thread-pool", config, crawler);

        System.gc();
        Thread.sleep(2000); // Delay between tests to allow metrics to be collected

        System.out.println("---------------------------------------------");
        System.out.println("Running test with virtual threads for tasks");
        System.out.println("---------------------------------------------");

        // Use virtual thread for tasks
        System.setProperty("job.task.pool.useVirtualThreads", "true");
        TestUtils.run("tasks-virtual-threads", config, crawler);

        Thread.sleep(2000); // allow metrics to be collected
    }
}
