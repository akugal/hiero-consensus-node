package org.hiero.metrics.api.export;

import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.core.MetricSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Predicate;

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
    public void export(List<MetricSnapshot> snapshots, OutputStream outputStream) throws IOException {

    }
}
