// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.api.export.MetricSnapshot;

/**
 * Abstract base class for {@link MetricsSnapshotsWriter} implementations.
 * Provides common functionality such as metric filtering and number formatting.
 */
public abstract class AbstractCachingMetricsSnapshotsWriter<M extends BaseMetricExportData>
        extends AbstractMetricsSnapshotsWriter {

    private final Map<MetricSnapshot, M> metricCache = new HashMap<>();

    public AbstractCachingMetricsSnapshotsWriter(Builder<?, ?> builder) {
        super(builder);
    }

    public void clearCache() {
        metricCache.clear();
    }

    @Override
    protected final void writeMetricSnapshot(Instant timestamp, MetricSnapshot metricSnapshot, OutputStream output)
            throws IOException {
        M metricExportData = metricCache.computeIfAbsent(metricSnapshot, this::buildMetricExportData);

        beforeMetricWrite(metricExportData, output);
        for (int i = 0; i < metricSnapshot.size(); i++) {
            DataPointSnapshot dataPointSnapshot = metricSnapshot.get(i);
            TemplateByteArray dataPointExportData = metricExportData.getAndUpdateDataPointExportData(dataPointSnapshot);
            writeDataPoint(timestamp, dataPointSnapshot, dataPointExportData, output);
        }
        afterMetricWrite(metricExportData, output);
    }

    protected byte[][] dataPointPlaceholder(byte[] valueBytes) {
        return new byte[][] {valueBytes};
    }

    protected void beforeMetricWrite(@NonNull M metricExportData, @NonNull OutputStream output) throws IOException {
        // nothing by default
    }

    protected void afterMetricWrite(@NonNull M metricExportData, @NonNull OutputStream output) throws IOException {
        // nothing by default
    }

    protected abstract void writeDataPoint(
            @NonNull Instant timestamp,
            @NonNull DataPointSnapshot dataPointSnapshot,
            @NonNull TemplateByteArray dataPointExportData,
            @NonNull OutputStream output)
            throws IOException;

    protected abstract M buildMetricExportData(MetricSnapshot metricSnapshot);
}
