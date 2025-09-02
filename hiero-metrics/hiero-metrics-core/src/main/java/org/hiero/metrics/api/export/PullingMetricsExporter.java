// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Type of metrics exporter that pulls metrics data from the metrics system on its own schedule.
 * Example of such exporter: Prometheus scrapper.
 *
 * @see PushingMetricsExporter
 */
public interface PullingMetricsExporter extends MetricsExporter {

    /**
     * Initialize the exporter with a supplier of {@link MetricsSnapshot}.
     * The supplier will be called by the exporter when it needs to pull metrics data.
     *
     * @param snapshotSupplier the supplier of {@link MetricsSnapshot}
     */
    void init(@NonNull Supplier<Optional<MetricsSnapshot>> snapshotSupplier);
}
