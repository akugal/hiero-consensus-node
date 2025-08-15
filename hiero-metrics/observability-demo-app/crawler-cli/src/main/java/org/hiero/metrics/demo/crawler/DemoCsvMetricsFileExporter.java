// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler;

import java.io.IOException;
import java.nio.file.Path;
import org.hiero.metrics.api.export.extension.CsvFileMetricsExporter;

public class DemoCsvMetricsFileExporter extends CsvFileMetricsExporter {

    public DemoCsvMetricsFileExporter() throws IOException {
        super("demo-metrics-csv", Path.of("out/metrics/metrics.csv"));
    }
}
