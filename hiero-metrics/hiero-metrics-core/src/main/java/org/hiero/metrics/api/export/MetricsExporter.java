// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Base interface for metrics exporters.
 * Metrics exporter able to handle {@link MetricsSnapshot} into specific destination.
 */
public interface MetricsExporter {

    /**
     * @return the name of the exporter, never {@code null}
     */
    @NonNull
    String getName();
}
