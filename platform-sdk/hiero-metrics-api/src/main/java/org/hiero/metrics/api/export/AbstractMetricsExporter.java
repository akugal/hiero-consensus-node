package org.hiero.metrics.api.export;

import org.hiero.metrics.api.core.MetricMetadata;

import java.text.DecimalFormat;
import java.util.function.Predicate;

public abstract class AbstractMetricsExporter implements MetricsExporter {

    private static final String DEFAULT_DECIMAL_FORMAT = "#.####";
    private static final Predicate<MetricMetadata> ALLOW_ALL = metadata -> true;

    protected final Predicate<MetricMetadata> filterMetrics;
    protected final DecimalFormat formatter;

    public AbstractMetricsExporter(Predicate<MetricMetadata> filterMetrics, String decimalFormat) {
        this.filterMetrics = filterMetrics;
        this.formatter = new DecimalFormat(decimalFormat);
    }

    public AbstractMetricsExporter(String doubleFormat) {
        this(ALLOW_ALL, doubleFormat);
    }

    public AbstractMetricsExporter() {
        this(ALLOW_ALL, DEFAULT_DECIMAL_FORMAT);
    }
}
