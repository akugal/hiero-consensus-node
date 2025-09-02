// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import java.time.Instant;
import java.util.List;

/**
 * Immutable snapshot of metrics at a specific time.
 *
 * @param snapshots   list of metrics snapshots
 * @param createdTime time at which snapshot is taken
 */
public record MetricsSnapshot(List<MetricSnapshot> snapshots, Instant createdTime) {}
