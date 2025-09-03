// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import java.text.DecimalFormat;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.MetricMetadata;

/**
 * Abstract base class for {@link MetricsSnapshotsWriter} implementations.
 * Provides common functionality such as metric filtering and number formatting.
 */
public abstract class AbstractMetricsSnapshotsWriter implements MetricsSnapshotsWriter {

    public static final String DEFAULT_DECIMAL_FORMAT = "#.####";
    public static final Predicate<MetricMetadata> ALLOW_ALL = metadata -> true;

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
