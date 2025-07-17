// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot.extension;

import java.text.DecimalFormat;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.MetricMetadata;

public abstract class AbstractMetricsSnapshotsWriter implements MetricsSnapshotsWriter {

    private static final String DEFAULT_DECIMAL_FORMAT = "#.####";
    private static final Predicate<MetricMetadata> ALLOW_ALL = metadata -> true;

    protected final Predicate<MetricMetadata> filterMetrics;
    protected final DecimalFormat formatter;

    public AbstractMetricsSnapshotsWriter(Predicate<MetricMetadata> filterMetrics, String decimalFormat) {
        this.filterMetrics = filterMetrics;
        this.formatter = new DecimalFormat(decimalFormat);
    }

    public AbstractMetricsSnapshotsWriter(String doubleFormat) {
        this(ALLOW_ALL, doubleFormat);
    }

    public AbstractMetricsSnapshotsWriter() {
        this(ALLOW_ALL, DEFAULT_DECIMAL_FORMAT);
    }
}
