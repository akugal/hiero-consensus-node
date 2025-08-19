// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job.metrics;

public record JobMetrics(JobProcessingMetrics processingMetrics, JobTaskMetrics concurrencyMetrics) {}
