// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension;

import static java.nio.file.StandardOpenOption.APPEND;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.export.PushingMetricsExporter;

public class CsvFileMetricsExporter implements PushingMetricsExporter {

    private final String name;
    private final Path filePath;
    private final CsvMetricsSnapshotsWriter writer;

    public CsvFileMetricsExporter(String name, Path filePath) throws IOException {
        this.name = name;
        this.filePath = filePath;
        writer = CsvMetricsSnapshotsWriter.DEFAULT;

        if (!Files.exists(filePath)) {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.createFile(filePath);
            try (OutputStream outputStream = Files.newOutputStream(filePath, APPEND)) {
                writer.writeHeaders(outputStream);
            }
        }
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
