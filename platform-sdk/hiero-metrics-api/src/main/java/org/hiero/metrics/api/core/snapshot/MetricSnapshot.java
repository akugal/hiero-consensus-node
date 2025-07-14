// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core.snapshot;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Comparator;
import java.util.List;
import org.hiero.metrics.api.core.MetricMetadata;

public record MetricSnapshot(@NonNull MetricMetadata metadata, @NonNull List<DataPointSnapshot> dataPoints) {

    public static final Comparator<MetricSnapshot> COMPARATOR =
            Comparator.comparing(snapshot -> snapshot.metadata.getFullName());
}
