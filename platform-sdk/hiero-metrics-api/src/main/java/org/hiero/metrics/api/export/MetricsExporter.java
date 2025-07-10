package org.hiero.metrics.api.export;

import org.hiero.metrics.api.core.MetricSnapshot;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface MetricsExporter {

    void export(List<MetricSnapshot> snapshots, OutputStream outputStream) throws IOException;
}
