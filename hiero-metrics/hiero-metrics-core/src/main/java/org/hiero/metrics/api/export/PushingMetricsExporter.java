// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;

public interface PushingMetricsExporter extends Exporter {

    void export(@NonNull MetricsSnapshot snapshot) throws IOException;
}
