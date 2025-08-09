// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.net.URI;
import org.hiero.metrics.demo.crawler.api.job.metrics.JobMetrics;
import org.hiero.metrics.demo.crawler.api.util.TypedMap;

public record JobResult(URI rootUri, TypedMap data, JobMetrics jobMetrics) {}
