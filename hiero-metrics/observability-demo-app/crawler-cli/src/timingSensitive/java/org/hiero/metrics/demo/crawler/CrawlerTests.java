// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler; // SPDX-License-Identifier: Apache-2.0

import java.time.Duration;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.random.TestRandomSchemeCrawler;
import org.junit.jupiter.api.Test;

public class CrawlerTests {

    @Test
    public void testWithCacheAndWithout() throws InterruptedException {
        SchemeCrawler crawler = new TestRandomSchemeCrawler(
                new IdempotentTimedProcessor(100, 300, 0.4), 0.8, new IdempotentTimedProcessor(50, 200, 0.1));

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(50))
                .withTimeout(Duration.ofSeconds(60))
                .withConcurrentUsers(4)
                .withThroughputPerSecond(4);

        System.out.println("Running test with cache enabled");
        // Provide spec for Guava cache
        // This will enable the cache with a maximum size of 1000 and expire entries after 20 seconds of access
        System.setProperty("cache.doc.guava.spec", "maximumSize=1000,expireAfterAccess=20s");
        TestUtils.run("cache-enabled", config, crawler);

        Thread.sleep(2000); // Delay between tests to allow metrics to be collected

        System.out.println("Running test with cache disabled");
        // Disable cache by setting an empty spec
        System.setProperty("cache.doc.guava.spec", "");
        TestUtils.run("cache-disabled", config, crawler);
    }

    @Test
    public void testWithThreadPoolAndVirtualThreadsForTasks() throws InterruptedException {
        SchemeCrawler crawler = new TestRandomSchemeCrawler(
                new IdempotentTimedProcessor(500, 1000, 0.2), 0.1, new IdempotentTimedProcessor(100, 200, 0.1));

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(50))
                .withTimeout(Duration.ofSeconds(60))
                .withConcurrentUsers(4)
                .withThroughputPerSecond(4);

        System.out.println("Running test with thread pool for tasks");
        // Use thread pool for tasks
        System.setProperty("job.task.pool.useVirtualThreads", "false");
        // set lower queue size to explore growing thread pool size
        System.setProperty("job.task.pool.queueSize", "200");
        TestUtils.run("tasks-thread-pool", config, crawler);

        Thread.sleep(2000); // Delay between tests to allow metrics to be collected

        System.out.println("Running test with virtual threads for tasks");
        // Use virtual thread for tasks
        System.setProperty("job.task.pool.useVirtualThreads", "true");
        TestUtils.run("tasks-virtual-threads", config, crawler);
    }
}
