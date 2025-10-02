// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import static org.hiero.metrics.api.export.extension.writer.WriterUtils.CLOSE_BRACKET;
import static org.hiero.metrics.api.export.extension.writer.WriterUtils.NEW_LINE;
import static org.hiero.metrics.api.export.extension.writer.WriterUtils.OPEN_BRACKET;
import static org.hiero.metrics.api.export.extension.writer.WriterUtils.SPACE;
import static org.hiero.metrics.api.export.extension.writer.WriterUtils.appendLabels;
import static org.hiero.metrics.api.export.extension.writer.WriterUtils.escape;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Iterator;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.api.export.MetricsSnapshot;

/**
 * A {@link MetricsSnapshotsWriter} implementation that writes metrics in the OpenMetrics text format.
 *
 * <p>See <a href="https://github.com/prometheus/OpenMetrics/blob/main/specification/OpenMetrics.md">OpenMetrics</a> for details.
 */
public class OpenMetricsSnapshotsWriter
        extends AbstractCachingMetricsSnapshotsWriter<OpenMetricsSnapshotsWriter.MetricExportData> {

    public static final OpenMetricsSnapshotsWriter DEFAULT =
            OpenMetricsSnapshotsWriter.builder().build();

    private static final EnumMap<MetricType, String> METRIC_TYPES = new EnumMap<>(MetricType.class);

    static {
        METRIC_TYPES.put(MetricType.UNKNOWN, "unknown");
        METRIC_TYPES.put(MetricType.GAUGE, "gauge");
        METRIC_TYPES.put(MetricType.COUNTER, "counter");
        METRIC_TYPES.put(MetricType.STATE_SET, "stateset");
        METRIC_TYPES.put(MetricType.INFO, "info");
    }

    private static final byte[] COUNTER_SUFFIX = "_total".getBytes(StandardCharsets.UTF_8);
    private static final byte[] INFO_SUFFIX = "_info".getBytes(StandardCharsets.UTF_8);
    ;
    private static final byte[] END = "# EOF\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DOUBLE_POSITIVE_INF = "+Inf".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DOUBLE_NEGATIVE_INF = "-Inf".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DOUBLE_NAN = "NaN".getBytes(StandardCharsets.UTF_8);

    private final boolean writeTimestamp;

    private OpenMetricsSnapshotsWriter(OpenMetricsSnapshotsWriter.Builder builder) {
        super(builder);
        this.writeTimestamp = builder.writeTimestamp;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    private static String getMetricTypeName(MetricType metricType) {
        String typeName = METRIC_TYPES.get(metricType);
        if (typeName == null) {
            throw new IllegalArgumentException("Unsupported metric type: " + metricType);
        }
        return typeName;
    }

    @Override
    protected void writeDataPoint(
            @NonNull Instant timestamp,
            @NonNull DataPointSnapshot dataPointSnapshot,
            @NonNull TemplateByteArray dataPointExportData,
            @NonNull OutputStream output)
            throws IOException {
        for (int i = 0; i < dataPointSnapshot.valuesSize(); i++) {
            byte[] valueBytes = convertValue(dataPointSnapshot.valueAt(i));

            byte[][] variables = new byte[3][];
            int varIdx = 0;

            if (dataPointSnapshot.valueClassifier() != null) {
                variables[varIdx++] = escape(dataPointSnapshot.valueTypeAt(i)).getBytes(StandardCharsets.UTF_8);
            }
            variables[varIdx++] = valueBytes;
            if (writeTimestamp) {
                variables[varIdx++] = convertTimestamp(timestamp.toEpochMilli());
            }

            Iterator<byte[]> iterator = dataPointExportData.iterator(varIdx, variables);
            while (iterator.hasNext()) {
                output.write(iterator.next());
            }
            output.write(NEW_LINE);
        }
    }

    @Override
    protected void beforeMetricWrite(@NonNull MetricExportData metricExportData, @NonNull OutputStream output)
            throws IOException {
        output.write(metricExportData.metricMetadataLines);
    }

    @Override
    protected void afterSnapshotsWrite(@NonNull MetricsSnapshot snapshots, @NonNull OutputStream output)
            throws IOException {
        output.write(END);
        super.afterSnapshotsWrite(snapshots, output);
    }

    @Override
    protected MetricExportData buildMetricExportData(MetricSnapshot metricSnapshot) {
        return new MetricExportData(metricSnapshot);
    }

    @Override
    protected byte[][] dataPointPlaceholder(byte[] valueBytes) {
        if (writeTimestamp) {
            byte[] timestampBytes = convertTimestamp(System.currentTimeMillis());
            return new byte[][] {valueBytes, timestampBytes};
        }
        return super.dataPointPlaceholder(valueBytes);
    }

    protected byte[] convertValue(double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return DOUBLE_POSITIVE_INF;
        } else if (value == Double.NEGATIVE_INFINITY) {
            return DOUBLE_NEGATIVE_INF;
        } else if (Double.isNaN(value)) {
            return DOUBLE_NAN;
        } else {
            return format(value).getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] convertTimestamp(long timestampMs) {
        String result = timestampMs / 1000L + ".";
        long ms = timestampMs % 1000;
        if (ms < 100) {
            result += "0";
        }
        if (ms < 10) {
            result += "0";
        }
        result += Long.toString(ms);
        return result.getBytes(StandardCharsets.UTF_8);
    }

    public class MetricExportData extends BaseMetricExportData {

        private final String metricName;
        private final byte[] metricMetadataLines;

        protected MetricExportData(MetricSnapshot metricSnapshot) {
            super(metricSnapshot);

            final MetricMetadata metadata = metricSnapshot.metadata();
            String metricName = metadata.name();
            String metricUnit = metadata.unit();

            if (!metricUnit.isEmpty()) {
                metricName += '_' + metricUnit;
            }

            this.metricName = metricName;

            final StringBuilder metadataLine = new StringBuilder();
            metadataLine
                    .append("# TYPE ")
                    .append(metricName)
                    .append(' ')
                    .append(getMetricTypeName(metadata.metricType()))
                    .append('\n');

            if (!metricUnit.isEmpty()) {
                metadataLine
                        .append("# UNIT ")
                        .append(metricName)
                        .append(' ')
                        .append(metricUnit)
                        .append('\n');
            }
            if (!metadata.description().isEmpty()) {
                metadataLine
                        .append("# HELP ")
                        .append(metricName)
                        .append(' ')
                        .append(escape(metadata.description()))
                        .append('\n');
            }

            metricMetadataLines = metadataLine.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        protected TemplateByteArray buildDataPointExportData(DataPointSnapshot dataPointSnapshot) {
            TemplateByteArray.Builder buffer = TemplateByteArray.builder();
            buffer.append(metricName);

            MetricType metricType = metricSnapshot().metadata().metricType();
            if (metricType == MetricType.COUNTER) {
                buffer.append(COUNTER_SUFFIX);
            } else if (metricType == MetricType.INFO) {
                buffer.append(INFO_SUFFIX);
            }

            appendLabels(buffer, metricSnapshot(), dataPointSnapshot, OPEN_BRACKET, CLOSE_BRACKET);
            buffer.append(SPACE);
            buffer.addPlaceholder();

            if (writeTimestamp) {
                buffer.append(SPACE);
                buffer.addPlaceholder();
            }

            return buffer.build();
        }
    }

    public static class Builder extends AbstractMetricsSnapshotsWriter.Builder<Builder, OpenMetricsSnapshotsWriter> {

        private boolean writeTimestamp = false;

        public Builder writeTimestamp() {
            this.writeTimestamp = true;
            return this;
        }

        @Override
        public OpenMetricsSnapshotsWriter build() {
            return new OpenMetricsSnapshotsWriter(this);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
