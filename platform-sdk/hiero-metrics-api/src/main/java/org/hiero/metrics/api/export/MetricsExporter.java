// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.hiero.metrics.api.core.snapshot.MetricSnapshot;

public interface MetricsExporter {

    void export(List<MetricSnapshot> snapshots, OutputStream outputStream) throws IOException;
}
