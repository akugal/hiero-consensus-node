// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job.metrics;

import org.hiero.metrics.demo.crawler.api.job.JobProcessingMetrics;

public record JobMetrics(JobProcessingMetrics processingMetrics, JobTaskMetrics concurrencyMetrics) {}
