// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.stream.Collectors;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.export.MetricSnapshot;

public interface SnapshotableMetricsRegistry extends MetricRegistry {

    @NonNull
    default List<MetricSnapshot> snapshot() {
        return getAll().stream()
                .filter(metric -> metric instanceof SnapshotableMetric)
                .map(metric -> new MetricSnapshot(metric.getMetadata(), ((SnapshotableMetric) metric).snapshot()))
                .sorted(MetricSnapshot.COMPARATOR)
                .collect(Collectors.toList());
    }
}
