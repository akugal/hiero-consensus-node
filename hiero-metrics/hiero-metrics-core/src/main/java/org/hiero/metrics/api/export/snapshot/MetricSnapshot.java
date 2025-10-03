// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.snapshot;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.ArrayAccessor;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricMetadata;

/**
 * Snapshot of the {@link org.hiero.metrics.api.core.Metric} and its data points at some point in time.
 * Implementations could be mutable for performance reasons, allowing to update the data point snapshots
 * in place with centralized snapshotting manager.
 */
public interface MetricSnapshot extends ArrayAccessor<DataPointSnapshot> {

    @NonNull
    MetricMetadata metadata();

    @NonNull
    List<Label> constantLabels();

    @NonNull
    List<String> dynamicLabelNames();
}
