// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.threadpool;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.hiero.metrics.demo.crawler.api.job.JobProcessingMetrics;

final class JobProcessingContext {

    private final long jobStartTime;
    private final AtomicLong fetchSuccessTotalTime = new AtomicLong();
    private final AtomicLong fetchErrorTotalTime = new AtomicLong();
    private final AtomicLong processSuccessTotalTime = new AtomicLong();

    private final AtomicInteger unsupportedUriCount = new AtomicInteger();
    private final AtomicInteger duplicateUriCount = new AtomicInteger();

    private final AtomicInteger fetchErrorsCount = new AtomicInteger();
    private final AtomicInteger fetchSuccessCount = new AtomicInteger();
    private final AtomicInteger processErrorsCount = new AtomicInteger();

    private final Set<URI> distinctUriSet = ConcurrentHashMap.newKeySet();

    JobProcessingContext() {
        this.jobStartTime = currentTime();
    }

    public long currentTime() {
        return System.nanoTime();
    }

    private Duration toDuration(long duration) {
        return Duration.ofNanos(duration);
    }

    public boolean encounterUri(URI uri) {
        if (distinctUriSet.add(uri)) {
            return true;
        } else {
            duplicateUriCount.incrementAndGet();
            return false;
        }
    }

    public void unsupportedUriSeen() {
        unsupportedUriCount.incrementAndGet();
    }

    public void fetchSuccess(long startTime) {
        fetchSuccessTotalTime.addAndGet(currentTime() - startTime);
        fetchSuccessCount.incrementAndGet();
    }

    public void fetchError(long startTime) {
        fetchErrorTotalTime.addAndGet(currentTime() - startTime);
        fetchErrorsCount.incrementAndGet();
    }

    public void processError() {
        processErrorsCount.incrementAndGet();
    }

    public void processFinished(long startTime) {
        processSuccessTotalTime.addAndGet(currentTime() - startTime);
    }

    public JobProcessingMetrics buildMetrics() {
        return new JobProcessingMetrics(
                toDuration(currentTime() - jobStartTime),
                toDuration(fetchSuccessTotalTime.get()),
                toDuration(fetchErrorTotalTime.get()),
                toDuration(processSuccessTotalTime.get()),
                distinctUriSet.size(),
                unsupportedUriCount.get(),
                duplicateUriCount.get(),
                fetchErrorsCount.get(),
                fetchSuccessCount.get(),
                processErrorsCount.get());
    }
}
