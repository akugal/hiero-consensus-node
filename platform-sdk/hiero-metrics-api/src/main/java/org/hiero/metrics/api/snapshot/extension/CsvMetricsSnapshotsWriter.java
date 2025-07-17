// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot.extension;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.snapshot.MetricSnapshot;

public class CsvMetricsSnapshotsWriter extends AbstractMetricsSnapshotsWriter {

    public CsvMetricsSnapshotsWriter(Predicate<MetricMetadata> filterMetrics, String decimalFormat) {
        super(filterMetrics, decimalFormat);
    }

    public CsvMetricsSnapshotsWriter(String doubleFormat) {
        super(doubleFormat);
    }

    public CsvMetricsSnapshotsWriter() {
        super();
    }

    @Override
    public void export(List<MetricSnapshot> snapshots, OutputStream outputStream) throws IOException {}
}
