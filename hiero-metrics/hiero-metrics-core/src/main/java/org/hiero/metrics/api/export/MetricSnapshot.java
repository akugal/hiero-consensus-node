// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.hiero.metrics.api.core.MetricMetadata;

/**
 * Immutable snapshot of the {@link org.hiero.metrics.api.core.Metric} and its data points at some point in time.
 *
 * @param metadata   metric metadata
 * @param dataPoints metric data point snapshots
 */
public record MetricSnapshot(@NonNull MetricMetadata metadata, @NonNull List<DataPointSnapshot> dataPoints) {

    public static final Comparator<MetricSnapshot> COMPARATOR =
            Comparator.comparing(snapshot -> snapshot.metadata.name());

    public MetricSnapshot(@NonNull MetricMetadata metadata, @NonNull List<DataPointSnapshot> dataPoints) {
        this.metadata = Objects.requireNonNull(metadata, "Metadata must not be null");
        Objects.requireNonNull(dataPoints, "Data points list must not be null");

        this.dataPoints = List.copyOf(dataPoints);
    }
}
