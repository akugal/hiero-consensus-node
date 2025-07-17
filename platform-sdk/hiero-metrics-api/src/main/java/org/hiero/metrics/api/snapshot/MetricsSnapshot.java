// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot;

import java.time.Instant;
import java.util.List;

public record MetricsSnapshot(List<MetricSnapshot> snapshots, Instant createdTime) {}
