// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler; // SPDX-License-Identifier: Apache-2.0

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobMetrics;

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

        int throughputDelay = config.throughputPerSecond() > 0 ? 1000 / config.throughputPerSecond() : 0;

        List<ScheduledJob> jobs = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        long timeoutMs = config.timeout().toMillis();

        for (TestJobSpec item : config.items()) {
            ScheduledJob scheduledJob = jobManager.schedule(
                    item.uri(),
                    item.timeout(),
                    item.depth(),
                    item.processors().toArray(new String[0]));
            jobs.add(scheduledJob);
            if (throughputDelay > 0) Thread.sleep(throughputDelay);
        }

        for (ScheduledJob job : jobs) {
            try {
                long remainingTime = timeoutMs - (System.currentTimeMillis() - startTime);
                if (remainingTime <= 0) {
                    System.out.println("Test timeout reached before all jobs completed");
                    jobManager.shutdown();
                    return;
                }

                JobResult result = job.getResult(Duration.ofMillis(remainingTime));
                printJobResults(job.getJobId(), result);

            } catch (JobException e) {
                System.out.println("Test timeout reached before all jobs completed");
                jobManager.shutdown();
                return;
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Test finished in {} ms", duration);
        System.out.println("Test finished in " + duration + " ms");

        jobManager.shutdown();
        jobManager.awaitTermination(config.timeout());
    }

    public static void printJobResults(int jobId, JobResult result) {
        JobMetrics metrics = result.jobMetrics();
        StringBuilder sb = new StringBuilder();

        sb.append("Job id=").append(jobId).append(" uri=").append(result.rootUri()).append("\n");
        sb.append("Duration:          ").append(metrics.processingMetrics().jobDuration().toMillis()).append(" ms\n");
        sb.append("Concurrency ratio: ").append(metrics.processingMetrics().concurrencyImprovementRatio()).append("x\n");
        sb.append("Tasks total:       ").append(metrics.concurrencyMetrics().totalTasksCount()).append("\n");
        sb.append("Tasks rejected:    ").append(metrics.concurrencyMetrics().rejectedTasksCount()).append("\n");
        sb.append("Task avg duration: ").append(metrics.concurrencyMetrics().taskExecutionAverageDuration().toMillis()).append(" ms\n");
        sb.append("Task avg delay:    ").append(metrics.concurrencyMetrics().tasksExecutionDelayTotalDuration().toMillis()).append(" ms\n");
        sb.append("Distinct uris:     ").append(metrics.processingMetrics().distinctUriCount()).append("\n");
        sb.append("Duplicate uris:    ").append(metrics.processingMetrics().duplicateUriCount()).append("\n");
        sb.append("Uri cache hit:     ").append(metrics.processingMetrics().getUriCacheHitCount()).append("\n");

        System.out.println(sb);
    }

    private static String toString(Duration duration) {
        return duration.toMillis() + " ms";
    }

    public static void simulateBlockingIO(Duration duration) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SCHEDULED_EXECUTOR.schedule(latch::countDown, duration.toMillis(), TimeUnit.MILLISECONDS);
        latch.await();
    }

    public static List<TestJobSpec> createRandomTestJobsSpecs(int count, int depth, Duration timeout) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than zero");
        }
        List<TestJobSpec> jobSpecs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            jobSpecs.add(new TestJobSpec("random://url/" + UUID.randomUUID())
                    .withTimeout(timeout)
                    .withDepth(depth));
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
