// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.export.MetricSnapshot;

/**
 * An extension of {@link MetricRegistry} that provides a method to take snapshots of all registered
 * metrics that implement {@link SnapshotableMetric}.
 */
public interface SnapshotableMetricsRegistry extends MetricRegistry {

    /**
     * Takes a snapshot of all registered metrics that implement {@link SnapshotableMetric}.
     * The snapshots are returned as a sorted unmodifiable list of {@link MetricSnapshot} objects.
     *
     * @return a sorted unmodifiable list of {@link MetricSnapshot} objects representing the snapshots of
     *         all registered snapshotable metrics
     */
    @NonNull
    default List<MetricSnapshot> snapshot() {
        return metrics().stream()
                .filter(metric -> metric instanceof SnapshotableMetric)
                .map(metric -> new MetricSnapshot(metric.metadata(), ((SnapshotableMetric) metric).snapshot()))
                .sorted(MetricSnapshot.COMPARATOR)
                .toList();
    }
}
