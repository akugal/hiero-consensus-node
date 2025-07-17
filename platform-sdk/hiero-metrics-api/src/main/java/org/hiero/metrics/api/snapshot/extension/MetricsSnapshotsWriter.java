// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot.extension;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.hiero.metrics.api.snapshot.MetricSnapshot;

public interface MetricsSnapshotsWriter {

    void export(List<MetricSnapshot> snapshots, OutputStream outputStream) throws IOException;
}
