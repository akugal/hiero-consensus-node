// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Supplier;
import org.hiero.metrics.api.export.AbstractMetricsExporter;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.export.PushingMetricsExporter;
import org.hiero.metrics.api.export.extension.writer.MetricsSnapshotsWriter;

/**
 * An adapter that allows using a {@link MetricsSnapshotsWriter} as a {@link PushingMetricsExporter}.
 * It uses a {@link Supplier} to provide the output stream for writing metrics snapshots.
 */
public final class PushingMetricsExporterWriterAdapter extends AbstractMetricsExporter
        implements PushingMetricsExporter {

    private final MetricsSnapshotsWriter writer;
    private final Supplier<OutputStream> streamSupplier;

    public PushingMetricsExporterWriterAdapter(
            @NonNull String name,
            @NonNull MetricsSnapshotsWriter writer,
            @NonNull Supplier<OutputStream> streamSupplier) {
        super(name);
        this.writer = Objects.requireNonNull(writer, "writer must not be null");
        this.streamSupplier = Objects.requireNonNull(streamSupplier, "output stream supplier must not be null");
    }

    @Override
    public void export(@NonNull MetricsSnapshot snapshot) throws IOException {
        try (OutputStream stream = streamSupplier.get()) {
            writer.write(snapshot, stream);
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}
