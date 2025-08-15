// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler;

import java.time.Duration;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.random.TestRandomSchemeCrawler;
import org.junit.jupiter.api.Test;

public class CrawlerTests {

    @Test
    public void testWithCacheAndWithout() throws InterruptedException {
        // adjust thread pool for jobs
        System.setProperty("job.pool.useVirtualThreads", "false");
        System.setProperty("job.pool.coreSize", "4");
        System.setProperty("job.pool.maxSize", "32");
        System.setProperty("job.pool.keepAliveSeconds", "60");
        System.setProperty("job.pool.queueSize", "0"); // unbounded

        // Adjust thread pool for job tasks
        System.setProperty("job.task.pool.useVirtualThreads", "false");
        System.setProperty("job.task.pool.coreSize", "8");
        System.setProperty("job.task.pool.maxSize", "64");
        System.setProperty("job.task.pool.keepAliveSeconds", "60");
        System.setProperty("job.task.pool.queueSize", "0"); // unbounded

        IdempotentTimedProcessor fetcher = new IdempotentTimedProcessor(200, 200, 0.0);
        IdempotentTimedProcessor processor = new IdempotentTimedProcessor(50, 50, 0.0);
        SchemeCrawler crawler = new TestRandomSchemeCrawler(fetcher, 0.8, 5, 5, processor);

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(150, 2, Duration.ofSeconds(30)))
                .withTimeout(Duration.ofSeconds(300))
                .withRampUpSeconds(4)
                .withThroughputPerSecond(2.3);

        // Provide spec for Guava cache
        // This will enable the cache with a maximum size of 100000 and expire entries after 30 seconds of access
        System.setProperty("cache.doc.guava.spec", "maximumSize=100000,expireAfterAccess=30s");
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

        IdempotentTimedProcessor fetcher = new IdempotentTimedProcessor(200, 200, 0.0);
        IdempotentTimedProcessor processor = new IdempotentTimedProcessor(50, 50, 0.0);
        SchemeCrawler crawler = new TestRandomSchemeCrawler(fetcher, 0.0, 5, 5, processor);

        TestConfig config = new TestConfig(TestUtils.createRandomTestJobsSpecs(150, 2, Duration.ofSeconds(30)))
                .withTimeout(Duration.ofSeconds(300))
                .withRampUpSeconds(4)
                .withThroughputPerSecond(2.3);

        // adjust thread pool for jobs
        System.setProperty("job.pool.useVirtualThreads", "false");
        System.setProperty("job.pool.coreSize", "4");
        System.setProperty("job.pool.maxSize", "32");
        System.setProperty("job.pool.keepAliveSeconds", "5");
        System.setProperty("job.pool.queueSize", "20"); // low queue size to see pool increase

        // Adjust thread pool for job tasks
        System.setProperty("job.task.pool.useVirtualThreads", "false");
        System.setProperty("job.task.pool.coreSize", "8");
        System.setProperty("job.task.pool.maxSize", "40");
        System.setProperty("job.task.pool.keepAliveSeconds", "3");
        System.setProperty("job.task.pool.queueSize", "200");
        TestUtils.run("tasks-thread-pool", config, crawler);

        // Use virtual thread for tasks
        System.setProperty("job.task.pool.useVirtualThreads", "true");
        TestUtils.run("tasks-virtual-threads", config, crawler);
    }

    @Test
    public void doMathOnConfig() {
        int processors = Runtime.getRuntime().availableProcessors();
        double jobPerSecond = 2.5;
        int taskGrowth = 5;
        int jobDepth = 2;
        int taskTimeMs = 250;

        // Now we can calculate different things

        System.out.println("--------------------------------");

        int jobTasksCount = (int) ((Math.pow(taskGrowth, jobDepth + 1) - 1) / (taskGrowth - 1));
        System.out.println("Job tasks count:        " + jobTasksCount);

        int jobExpectedTimeMs = (jobTasksCount * taskTimeMs / processors);
        System.out.println("Job expected time (ms): " + jobExpectedTimeMs);

        double tasksPerSec = jobPerSecond * jobTasksCount;
        System.out.println("Expected Tasks/second:  " + tasksPerSec);

        int availableTasksPerSec = 1000 * processors / taskTimeMs;
        System.out.println("Available Tasks/second:  " + availableTasksPerSec);

        System.out.println("--------------------------------");
    }
}
