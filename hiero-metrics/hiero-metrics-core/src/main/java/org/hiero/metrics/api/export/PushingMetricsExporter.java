// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;

/**
 * Type of metrics exporter that receives metrics data pushed to it by the metrics system on regular intervals.
 * Example of such exporter: CSV file.
 *
 * @see PullingMetricsExporter
 */
public interface PushingMetricsExporter extends MetricsExporter {

    /**
     * Export the given metrics snapshot to the destination.
     *
     * @param snapshot metrics snapshot to export
     * @throws IOException IO exception during export
     */
    void export(@NonNull MetricsSnapshot snapshot) throws IOException;
}
