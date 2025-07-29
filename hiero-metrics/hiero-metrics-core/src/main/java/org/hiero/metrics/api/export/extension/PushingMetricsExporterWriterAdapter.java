// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Supplier;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.export.PushingMetricsExporter;

public class PushingMetricsExporterWriterAdapter implements PushingMetricsExporter {

    private final String name;
    private final MetricsSnapshotsWriter writer;
    private final Supplier<OutputStream> streamSupplier;

    public PushingMetricsExporterWriterAdapter(
            String name, MetricsSnapshotsWriter writer, Supplier<OutputStream> streamSupplier) {
        this.name = name;
        this.writer = writer;
        this.streamSupplier = streamSupplier;
    }

    @Override
    public void export(@NonNull MetricsSnapshot snapshot) throws IOException {
        try (OutputStream stream = streamSupplier.get()) {
            writer.export(snapshot, stream);
        }
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
