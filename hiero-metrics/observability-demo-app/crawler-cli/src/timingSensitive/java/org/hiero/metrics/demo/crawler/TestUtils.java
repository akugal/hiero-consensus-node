// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler; // SPDX-License-Identifier: Apache-2.0

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

public class TestUtils {

    private static final Logger logger = LogManager.getLogger(TestUtils.class);

    private static final Random RANDOM = new Random();

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public static void enableConsoleLogging() {
        System.setProperty("log4j.configurationFile", "src/test/resources/log4j2-console.xml");
    }

    public static void run(String testName, TestConfig config, SchemeCrawler crawler) throws InterruptedException {
        JobManager jobManager = Utils.initializeJobManager(testName);
        jobManager.registerCrawler(crawler);
        run(testName, jobManager, config);
    }

    public static void run(String testName, JobManager jobManager, TestConfig config) throws InterruptedException {
        Objects.requireNonNull(testName, "testName cannot be null");
        Objects.requireNonNull(jobManager, "jobManager cannot be null");
        Objects.requireNonNull(config, "org.hiero.metrics.demo.crawler.TestConfig cannot be null");

        try (ExecutorService executor = Executors.newFixedThreadPool(config.concurrentUsers())) {
            int throughputDelay = config.throughputPerSecond() > 0 ? 1000 / config.throughputPerSecond() : 0;

            List<Future<ScheduledJob>> submissions = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            for (TestJobSpec item : config.items()) {
                Future<ScheduledJob> submit = executor.submit(() -> jobManager.schedule(
                        item.uri(),
                        item.timeout(),
                        item.depth(),
                        item.processors().toArray(new String[0])));
                submissions.add(submit);
                if (throughputDelay > 0) Thread.sleep(throughputDelay);
            }

            for (Future<ScheduledJob> submission : submissions) {
                try {
                    submission.get().getResult();
                } catch (ExecutionException e) {
                    System.out.println("Job failed: " + e.getCause().getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Test finished in {} ms", duration);
            System.out.println("Test finished in " + duration + " ms");

            jobManager.shutdown();
            jobManager.awaitTermination(config.timeout());
            executor.shutdown();
        }
    }

    public static void simulateBlockingIO(Duration duration) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SCHEDULED_EXECUTOR.schedule(latch::countDown, duration.toMillis(), TimeUnit.MILLISECONDS);
        latch.await();
    }

    public static List<TestJobSpec> createRandomTestJobsSpecs(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than zero");
        }
        List<TestJobSpec> jobSpecs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            jobSpecs.add(new TestJobSpec("random://url/" + UUID.randomUUID())
                    .withTimeout(Duration.ofSeconds(60))
                    .withDepth((int) randomLong(2, 5)));
        }
        return jobSpecs;
    }

    public static long randomDeviation(long value, double maxDeviation) {
        if (maxDeviation == StatUtils.ZERO) {
            return value;
        }
        if (maxDeviation < StatUtils.ZERO || maxDeviation > StatUtils.ONE) {
            throw new IllegalArgumentException("Max deviation must be between 0 and 1");
        }

        double deviation = value * maxDeviation;
        long min = (long) Math.floor(value - deviation);
        long max = (long) Math.ceil(value + deviation);
        return RANDOM.nextLong(min, max);
    }

    public static long randomLong(long min, long max) {
        if (min == max) {
            return min;
        }
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than max");
        }
        return RANDOM.nextLong(min, max);
    }
}
