// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.export.DataPointSnapshot;

/**
 * A metric that can produce a snapshot of its current data points.
 */
public interface SnapshotableMetric extends Metric {

    /**
     * Create a snapshot of the current metric data points.
     *
     * @return a list of data point snapshots
     */
    @NonNull
    List<DataPointSnapshot> snapshot();
}
