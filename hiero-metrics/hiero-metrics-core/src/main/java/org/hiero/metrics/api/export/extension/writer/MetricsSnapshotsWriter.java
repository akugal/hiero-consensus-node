// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import org.hiero.metrics.api.export.MetricsSnapshot;

/**
 * Interface for writing {@link MetricsSnapshot} to an output stream.
 */
public interface MetricsSnapshotsWriter {

    /**
     * Writes {@link MetricsSnapshot} to the provided output stream.
     *
     * @param outputStream the output stream to write to
     * @throws IOException if an I/O error occurs
     */
    void write(@NonNull MetricsSnapshot snapshot, OutputStream outputStream) throws IOException;
}
