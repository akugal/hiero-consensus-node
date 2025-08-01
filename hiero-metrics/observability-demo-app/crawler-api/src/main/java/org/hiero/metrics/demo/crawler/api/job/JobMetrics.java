// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public record JobMetrics(
        long durationMs,
        long fetchSuccessTotalTimeMs,
        long fetchErrorTotalTimeMs,
        long processSuccessTotalTimeMs,
        int distinctUriCount,
        int unsupportedUriCount,
        int duplicateUriCount,
        int fetchErrorsCount,
        int fetchSuccessCount,
        int processErrorsCount) {

    public int getUriCacheHitCount() {
        return distinctUriCount - fetchErrorsCount - fetchSuccessCount;
    }

    public int parallelImprovementRatio() {
        if (durationMs == 0) {
            return 0;
        }
        return (int) ((fetchSuccessTotalTimeMs + fetchErrorTotalTimeMs + processSuccessTotalTimeMs) / durationMs);
    }

    public static JobMetrics.Builder builder() {
        return new JobMetrics.Builder();
    }

    public static class Builder {

        private final long startTimeMs = System.currentTimeMillis();

        private final AtomicLong fetchSuccessTotalTimeMs = new AtomicLong();
        private final AtomicLong fetchErrorTotalTimeMs = new AtomicLong();
        private final AtomicLong processSuccessTotalTimeMs = new AtomicLong();

        private final AtomicInteger unsupportedUriCount = new AtomicInteger();
        private final AtomicInteger duplicateUriCount = new AtomicInteger();

        private final AtomicInteger fetchErrorsCount = new AtomicInteger();
        private final AtomicInteger fetchSuccessCount = new AtomicInteger();
        private final AtomicInteger processErrorsCount = new AtomicInteger();

        private final Set<URI> distinctUriSet = ConcurrentHashMap.newKeySet();

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

        public void fetchSuccess(long elapsedMs) {
            fetchSuccessTotalTimeMs.addAndGet(elapsedMs);
            fetchSuccessCount.incrementAndGet();
        }

        public void fetchError(long startTime) {
            fetchErrorTotalTimeMs.addAndGet(startTime);
            fetchErrorsCount.incrementAndGet();
        }

        public void processError() {
            processErrorsCount.incrementAndGet();
        }

        public void processFinished(long startTime) {
            processSuccessTotalTimeMs.addAndGet(startTime);
        }

        public JobMetrics build() {
            return new JobMetrics(
                    System.currentTimeMillis() - startTimeMs,
                    fetchSuccessTotalTimeMs.get(),
                    fetchErrorTotalTimeMs.get(),
                    processSuccessTotalTimeMs.get(),
                    distinctUriSet.size(),
                    unsupportedUriCount.get(),
                    duplicateUriCount.get(),
                    fetchErrorsCount.get(),
                    fetchSuccessCount.get(),
                    processErrorsCount.get());
        }
    }
}
