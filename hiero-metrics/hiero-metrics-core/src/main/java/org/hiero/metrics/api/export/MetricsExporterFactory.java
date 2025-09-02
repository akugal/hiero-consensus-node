// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import com.swirlds.config.api.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface MetricsExporterFactory {

    @NonNull
    MetricsExporter createExporter(@NonNull Configuration configuration) throws Exception;
}
