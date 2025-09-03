// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler;

import com.swirlds.config.api.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.nio.file.Path;
import org.hiero.metrics.api.export.MetricsExporter;
import org.hiero.metrics.api.export.MetricsExporterFactory;
import org.hiero.metrics.api.export.extension.CsvFileMetricsExporter;

public class DemoCsvMetricsFileExporterFactory implements MetricsExporterFactory {

    @NonNull
    @Override
    public MetricsExporter createExporter(@NonNull Configuration configuration) throws Exception {
        return new CsvFileMetricsExporter("demo-metrics-csv", Path.of("out/metrics/metrics.csv"));
    }
}
