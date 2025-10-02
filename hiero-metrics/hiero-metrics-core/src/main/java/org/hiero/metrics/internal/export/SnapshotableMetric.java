// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import org.hiero.metrics.api.core.Metric;

/**
 * A metric that can produce a snapshot of its current data points.
 */
public interface SnapshotableMetric extends Metric {

    UpdatableMetricSnapshot<?> snapshot();
}
