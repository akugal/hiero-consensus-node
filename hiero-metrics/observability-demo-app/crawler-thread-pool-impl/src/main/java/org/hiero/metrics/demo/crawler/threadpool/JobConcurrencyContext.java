// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hiero.metrics.demo.crawler.api.exception.JobException;
import org.hiero.metrics.demo.crawler.api.exception.JobTimeoutException;
import org.hiero.metrics.demo.crawler.api.job.JobConcurrencyMetrics;

final class JobConcurrencyContext {

    private static final Logger logger = LogManager.getLogger(JobConcurrencyContext.class);

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final List<Future<?>> submittedTasksFutures = new ArrayList<>();

    private final AtomicLong taskExecutionDelayTotal = new AtomicLong(0);
    private final AtomicLong taskExecutionTotal = new AtomicLong(0);
    private final AtomicInteger totalTasksCount = new AtomicInteger(0);

    private final AtomicInteger submittedTasks = new AtomicInteger(0);
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicInteger rejectedTasksCount = new AtomicInteger(0);

    private final long jobSubmitTime;
    private long jobStartTime;

    public JobConcurrencyContext() {
        this.jobSubmitTime = currentTime();
    }

    public long currentTime() {
        return System.nanoTime();
    }

    private Duration toDuration(long duration) {
        return Duration.ofNanos(duration);
    }

    public long jobStarted() {
        jobStartTime = currentTime();
        return jobStartTime;
    }

    public void taskRejected() {
        rejectedTasksCount.incrementAndGet();
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            logger.info("Cancelling job");

            synchronized (submittedTasksFutures) {
                for (Future<?> future : submittedTasksFutures) {
                    future.cancel(true);
                }
            }
            submittedTasks.set(0);
            activeTasks.set(0);
        }
    }

    public void taskSubmitted(Future<?> future) {
        totalTasksCount.incrementAndGet();
        submittedTasks.incrementAndGet();
        synchronized (submittedTasksFutures) {
            submittedTasksFutures.add(future);
        }
    }

    public long taskStarted(long submittedTime) {
        submittedTasks.decrementAndGet();
        activeTasks.incrementAndGet();
        long startTime = currentTime();
        taskExecutionDelayTotal.addAndGet(startTime - submittedTime);
        return startTime;
    }

    public void taskEnded(long taskStartedTime) {
        activeTasks.decrementAndGet();
        taskExecutionTotal.addAndGet(currentTime() - taskStartedTime);

        // periodically clean up completed futures to avoid memory leaks
        if (submittedTasksFutures.size() > 100) {
            synchronized (submittedTasksFutures) {
                submittedTasksFutures.removeIf(Future::isDone);
            }
        }

        if (remainingTasks() == 0) {
            synchronized (this) {
                notify();
            }
        }
    }

    public synchronized void waitForTasksToComplete(Duration timeout) throws JobException {
        while (!isCancelled() && remainingTasks() > 0) {
            if (Thread.currentThread().isInterrupted()) {
                cancel();
                return;
            }

            long elapsedTime = currentTime() - jobSubmitTime;
            long remainingTime = timeout.minus(toDuration(elapsedTime)).toMillis();

            if (remainingTime <= 0) {
                cancel();
                throw new JobTimeoutException("Job timed out after " + timeout + ". Active tasks: " + activeTasks.get()
                        + ". Submitted tasks: " + submittedTasks.get());
            }

            try {
                logger.info(
                        "Awaiting for tasks to complete. Remining tasks: {}, Remaining time: {} ms",
                        remainingTasks(),
                        remainingTime);
                wait(remainingTime); // Wait only for remaining time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                cancel();
                throw new JobException("Job interrupted", e);
            }
        }
    }

    private int remainingTasks() {
        return activeTasks.get() + submittedTasks.get();
    }

    public JobConcurrencyMetrics buildMetrics() {
        if (jobStartTime == 0) {
            throw new IllegalStateException("Job has not been started");
        }
        return new JobConcurrencyMetrics(
                toDuration(jobStartTime - jobSubmitTime),
                totalTasksCount.get(),
                rejectedTasksCount.get(),
                toDuration(taskExecutionDelayTotal.get()),
                toDuration(taskExecutionTotal.get()));
    }
}
