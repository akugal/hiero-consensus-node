// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

public record JobMetrics(JobProcessingMetrics processingMetrics, JobConcurrencyMetrics concurrencyMetrics) {}
