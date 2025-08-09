// SPDX-License-Identifier: Apache-2.0

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.Utils;
import org.hiero.metrics.demo.crawler.api.job.JobManager;
import org.hiero.metrics.demo.crawler.api.job.ScheduledJob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class StressTestRunner {

    private static final Logger logger = LogManager.getLogger(StressTestRunner.class);

    private StressTestRunner() {
        // Prevent instantiation
    }

    public static void run(String testName, TestConfig config) throws InterruptedException {
        Objects.requireNonNull(testName, "testName cannot be null");
        Objects.requireNonNull(config, "TestConfig cannot be null");

        JobManager jobManager = Utils.initializeJobManager(testName);

        try (ExecutorService executor = Executors.newFixedThreadPool(config.concurrentUsers())) {
            int throughputDelay = config.throughputPerSecond() > 0 ? 1000 / config.throughputPerSecond() : 0;

            List<Future<ScheduledJob>> submissions = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            for (TestItem item : config.items()) {
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
}
