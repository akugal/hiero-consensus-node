// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.core.snapshot.MetricSnapshot;

public class CsvMetricsExporter extends AbstractMetricsExporter {

    public CsvMetricsExporter(Predicate<MetricMetadata> filterMetrics, String decimalFormat) {
        super(filterMetrics, decimalFormat);
    }

    public CsvMetricsExporter(String doubleFormat) {
        super(doubleFormat);
    }

    public CsvMetricsExporter() {
        super();
    }

    @Override
    public void export(List<MetricSnapshot> snapshots, OutputStream outputStream) throws IOException {}
}
