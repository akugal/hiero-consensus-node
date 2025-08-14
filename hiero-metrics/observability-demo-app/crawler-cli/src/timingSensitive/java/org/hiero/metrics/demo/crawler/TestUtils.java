// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler;

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
import org.hiero.metrics.api.stat.StatUtils;
import org.hiero.metrics.demo.crawler.api.document.SchemeCrawler;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.exception.JobTimeoutException;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.JobResult;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobMetrics;

public class TestUtils {

    private static final Random RANDOM = new Random();

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public static void run(String testName, TestConfig config, SchemeCrawler crawler) throws InterruptedException {
        JobManager jobManager = Utils.initializeJobManager(testName);
        jobManager.registerCrawler(crawler);
        run(testName, jobManager, config);
    }

    public static void run(String testName, JobManager jobManager, TestConfig config) throws InterruptedException {
        Objects.requireNonNull(testName, "testName cannot be null");
        Objects.requireNonNull(jobManager, "jobManager cannot be null");
        Objects.requireNonNull(config, "org.hiero.metrics.demo.crawler.TestConfig cannot be null");

        System.out.println();
        System.out.println("---------------------------------------------");
        System.out.println("▶️ Running test: " + testName);
        System.out.println("Config: ");
        System.out.println("  jobs = " + config.items().size());
        System.out.println("  timeout = " + toString(config.timeout()));
        System.out.println("  throughput = " + config.throughputPerSecond() + " per second");
        System.out.println("  ramp-up = " + config.rampUpSeconds() + " seconds, starting with 5x slower throughput");
        System.out.println("---------------------------------------------");

        int rampUpMs = config.rampUpSeconds() * 1000;
        int finalDelay = config.throughputPerSecond() > 0 ? (int) (1000 / config.throughputPerSecond()) : 0;
        int initialDelay = finalDelay * 5; // 5x slower at start

        List<ScheduledJob> jobs = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        long timeoutMs = config.timeout().toMillis();

        for (TestJobSpec item : config.items()) {
            ScheduledJob scheduledJob = jobManager.schedule(
                    item.uri(), item.timeout(), item.depth(), item.processors().toArray(new String[0]));
            jobs.add(scheduledJob);

            // Calculate delay based on ramp-up progress
            int delay;
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (rampUpMs > 0 && elapsedTime < rampUpMs) {
                // Linear ramp-up from initialDelay to finalDelay
                double progress = (double) elapsedTime / rampUpMs;
                delay = (int) (initialDelay - (progress * (initialDelay - finalDelay)));
            } else {
                delay = finalDelay;
            }

            if (delay > 0) Thread.sleep(delay);
        }

        long remainingTime;
        for (ScheduledJob job : jobs) {
            try {
                remainingTime = timeoutMs - (System.currentTimeMillis() - startTime);
                if (remainingTime <= 0) {
                    System.out.println("⌛ Test timeout reached before all jobs completed");
                    jobManager.shutdown();
                    return;
                }

                JobResult result = job.getResult(Duration.ofMillis(remainingTime));
                printJobResults(job.getJobId(), result);

            } catch (JobTimeoutException te) {
                System.out.println("⌛ Test timeout reached before all jobs completed");
                jobManager.shutdown();
                return;
            } catch (JobException e) {
                System.out.println("❌ Job Failed id=" + job.getJobId() + " : " + e.getMessage());
            }
        }

        remainingTime = timeoutMs - (System.currentTimeMillis() - startTime);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("✅ Test '" + testName + "'  finished in " + duration + " ms");
        System.out.println("---------------------------------------------");

        jobManager.shutdown();
        jobManager.awaitTermination(Duration.ofMillis(remainingTime)); // should be finished already

        Thread.sleep(3000); // allow metrics to be collected
        System.gc();
    }

    public static void printJobResults(int jobId, JobResult result) {
        JobMetrics metrics = result.jobMetrics();

        String sb = "✔️ Job id=" + jobId + " uri=" + result.rootUri() + "\n" + "  Duration:           "
                + metrics.processingMetrics().jobDuration().toMillis() + " ms\n" + "  Concurrency factor: "
                + metrics.processingMetrics().concurrencyFactor() + "x\n" + "  Tasks total:        "
                + metrics.concurrencyMetrics().totalTasksCount() + "\n" + "  Tasks rejected:     "
                + metrics.concurrencyMetrics().rejectedTasksCount() + "\n" + "  Task avg duration:  "
                + metrics.concurrencyMetrics().taskExecutionAverageDuration().toMillis() + " ms\n"
                + "  Task avg delay:     "
                + metrics.concurrencyMetrics()
                        .tasksExecutionDelayTotalDuration()
                        .toMillis() + " ms\n" + "  Distinct uris:      "
                + metrics.processingMetrics().distinctUriCount() + "\n" + "  Duplicate uris:     "
                + metrics.processingMetrics().duplicateUriCount() + "\n" + "  Uri cache hit:      "
                + metrics.processingMetrics().getUriCacheHitCount() + "\n";

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
