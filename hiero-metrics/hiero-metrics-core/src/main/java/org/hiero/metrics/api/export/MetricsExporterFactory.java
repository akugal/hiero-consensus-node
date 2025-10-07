// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import com.swirlds.config.api.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A factory for creating {@link MetricsExporter} instances based on the provided configuration.
 * Exporters created by this factory must be either {@link PullingMetricsExporter} or {@link PushingMetricsExporter}.
 */
public interface MetricsExporterFactory {

    /**
     * Creates a new {@link MetricsExporter} instance based on the provided configuration.
     *
     * @param configuration the configuration to use for creating the exporter, must not be {@code null}
     * @return a new instance of {@link MetricsExporter}, never {@code null}
     * @throws Exception if there is an error during the creation of the exporter
     */
    @NonNull
    MetricsExporter createExporter(@NonNull Configuration configuration) throws Exception;
}
