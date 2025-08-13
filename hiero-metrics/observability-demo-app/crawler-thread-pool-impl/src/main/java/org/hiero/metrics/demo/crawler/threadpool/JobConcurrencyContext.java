// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.exception.JobTimeoutException;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobTaskMetrics;

final class JobConcurrencyContext {

    private final Duration timeout;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final List<Future<?>> submittedTasksFutures = new ArrayList<>();

    private final AtomicLong taskExecutionTimeDelayTotal = new AtomicLong(0);
    private final AtomicLong taskExecutionTimeTotal = new AtomicLong(0);
    private final AtomicInteger totalTasksCount = new AtomicInteger(0);
    private final AtomicInteger rejectedTasksCount = new AtomicInteger(0);

    private final Logger logger;
    private final Phaser phaser = new Phaser();

    public JobConcurrencyContext(Duration timeout, Logger logger) {
        this.timeout = timeout;
        this.logger = logger;
    }

    public long currentTime() {
        return System.nanoTime();
    }

    private Duration toDuration(long duration) {
        return Duration.ofNanos(duration);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void taskRejected() {
        rejectedTasksCount.incrementAndGet();
    }

    public void taskSubmitted(Future<?> future) {
        totalTasksCount.incrementAndGet();
        synchronized (submittedTasksFutures) {
            submittedTasksFutures.add(future);
        }
    }

    public void registerNewTask() {
        phaser.register();
    }

    public void deregisterTask() {
        phaser.arrive();
    }

    public long taskStarted(long submittedTime) {
        long startTime = currentTime();
        taskExecutionTimeDelayTotal.addAndGet(startTime - submittedTime);
        return startTime;
    }

    public void taskEnded(long taskStartedTime) {
        taskExecutionTimeTotal.addAndGet(currentTime() - taskStartedTime);

        // periodically clean up completed futures to avoid memory leaks
        if (submittedTasksFutures.size() > 100) {
            cleanUpDoneTasks();
        }

        deregisterTask();
    }

    public void waitForTasksToComplete() throws JobException {
        try {
            phaser.awaitAdvanceInterruptibly(0, timeout.toMillis(), TimeUnit.MILLISECONDS);
            cleanUpDoneTasks();
        } catch (InterruptedException e) {
            logger.warn("Job interrupted.", e);
            cancel();
            throw new JobException("Job interrupted", e);
        } catch (TimeoutException e) {
            logger.error(
                    "Job timeout. timeoutMs={}, unarrived={}, arrived={}",
                    timeout.toMillis(),
                    phaser.getUnarrivedParties(),
                    phaser.getArrivedParties());
            cancel();
            throw new JobTimeoutException(
                    "Timeout reached (" + timeout.toMillis() + " ms) waiting for tasks to complete");
        }
    }

    private void cleanUpDoneTasks() {
        synchronized (submittedTasksFutures) {
            submittedTasksFutures.removeIf(Future::isDone);
        }
    }

    private void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            logger.info("Job cancelled. Cancelling {} submitted tasks.", submittedTasksFutures.size());

            synchronized (submittedTasksFutures) {
                for (Future<?> future : submittedTasksFutures) {
                    future.cancel(true);
                }
            }
        }
    }

    public JobTaskMetrics buildMetrics() {
        return new JobTaskMetrics(
                totalTasksCount.get(),
                rejectedTasksCount.get(),
                toDuration(taskExecutionTimeDelayTotal.get()),
                toDuration(taskExecutionTimeTotal.get()));
    }
}
