// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.openmetrics;

import com.swirlds.config.api.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import org.hiero.metrics.api.export.MetricsExporter;
import org.hiero.metrics.api.export.MetricsExporterFactory;
import org.hiero.metrics.openmetrics.config.OpenMetricsHttpEndpointConfig;

/**
 * Implementation of {@link MetricsExporterFactory} for creating OpenMetrics HTTP endpoint exporters.
 */
public class OpenMetricsHttpEndpointFactory implements MetricsExporterFactory {

    @NonNull
    @Override
    public MetricsExporter createExporter(@NonNull Configuration configuration) throws IOException {
        return new OpenMetricsHttpEndpoint(configuration.getConfigData(OpenMetricsHttpEndpointConfig.class));
    }
}
