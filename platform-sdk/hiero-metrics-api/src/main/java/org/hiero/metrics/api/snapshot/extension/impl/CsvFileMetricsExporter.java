package org.hiero.metrics.api.snapshot.extension.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.snapshot.MetricsSnapshot;
import org.hiero.metrics.api.snapshot.PushingMetricsExporter;
import org.hiero.metrics.api.snapshot.extension.CsvMetricsSnapshotsWriter;
import org.hiero.metrics.api.snapshot.extension.MetricsSnapshotsWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;

public class CsvFileMetricsExporter implements PushingMetricsExporter {

    private final String name;
    private final Path filePath;
    private final MetricsSnapshotsWriter writer;

    public CsvFileMetricsExporter(String name, Path filePath) {
        this.name = name;
        this.filePath = filePath;
        writer = CsvMetricsSnapshotsWriter.DEFAULT;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void export(@NonNull MetricsSnapshot snapshot) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(filePath, APPEND)) {
            writer.export(snapshot, outputStream);
        }
    }
}
