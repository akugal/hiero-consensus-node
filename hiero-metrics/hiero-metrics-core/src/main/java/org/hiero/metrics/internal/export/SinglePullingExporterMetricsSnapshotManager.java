// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import org.hiero.metrics.api.export.PullingMetricsExporter;

public class SinglePullingExporterMetricsSnapshotManager extends AbstractMetricsSnapshotManager {

    private final PullingMetricsExporter exporter;

    public SinglePullingExporterMetricsSnapshotManager(@NonNull PullingMetricsExporter exporter) {
        super();
        this.exporter = Objects.requireNonNull(exporter, "exporter must not be null");
    }

    @Override
    protected void init() {
        exporter.init(this::takeSnapshot);
    }

    @Override
    public boolean hasRunningSnapshotThread() {
        return false;
    }
}
