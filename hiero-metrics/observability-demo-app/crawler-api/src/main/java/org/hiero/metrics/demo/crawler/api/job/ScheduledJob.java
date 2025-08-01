// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.job;

import java.util.concurrent.Future;

public record ScheduledJob(int jobId, Future<JobResult> future) {}
