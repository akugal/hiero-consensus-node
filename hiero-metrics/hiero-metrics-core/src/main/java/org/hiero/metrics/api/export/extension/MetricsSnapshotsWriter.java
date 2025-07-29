// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import org.hiero.metrics.api.export.MetricsSnapshot;

public interface MetricsSnapshotsWriter {

    void export(@NonNull MetricsSnapshot snapshot, OutputStream outputStream) throws IOException;
}
