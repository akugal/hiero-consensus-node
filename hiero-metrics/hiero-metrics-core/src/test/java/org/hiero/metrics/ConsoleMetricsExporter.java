package org.hiero.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.export.extension.PushingMetricsExporterWriterAdapter;
import org.hiero.metrics.api.export.extension.writer.MetricsSnapshotsWriter;

import java.io.IOException;
import java.io.OutputStream;

public class ConsoleMetricsExporter extends PushingMetricsExporterWriterAdapter {

    public ConsoleMetricsExporter(@NonNull MetricsSnapshotsWriter writer) {
        super("console", writer);
    }

    @Override
    protected OutputStream openStream() throws IOException {
        return System.out;
    }
}
