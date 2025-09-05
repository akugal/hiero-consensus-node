// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import org.hiero.metrics.api.export.PullingMetricsExporter;

public class SinglePullingExporterMetricsExportManager extends AbstractMetricsExportManager {

    private final PullingMetricsExporter exporter;

    public SinglePullingExporterMetricsExportManager(@NonNull String name, @NonNull PullingMetricsExporter exporter) {
        super(name);
        this.exporter = Objects.requireNonNull(exporter, "exporter must not be null");
    }

    @Override
    protected void init() {
        super.init();
        exporter.init(this::takeSnapshot);
    }

    @Override
    public boolean hasRunningExportThread() {
        return false;
    }
}
