// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.api.export.MetricsSnapshot;

public class OpenMetricsSnapshotsWriter extends AbstractMetricsSnapshotsWriter {

    private static final EnumMap<MetricType, String> METRIC_TYPES = new EnumMap<>(MetricType.class);

    static {
        METRIC_TYPES.put(MetricType.UNKNOWN, "unknown");
        METRIC_TYPES.put(MetricType.GAUGE, "gauge");
        METRIC_TYPES.put(MetricType.COUNTER, "counter");
        METRIC_TYPES.put(MetricType.STATE_SET, "stateset");
        METRIC_TYPES.put(MetricType.INFO, "info");
    }

    private static final String COUNTER_SUFFIX = "_total";
    private static final String INFO_SUFFIX = "_info";

    private final boolean writeTimestamp;

    public OpenMetricsSnapshotsWriter(
            Predicate<MetricMetadata> filterMetrics, String decimalFormat, boolean writeTimestamp) {
        super(filterMetrics, decimalFormat);
        this.writeTimestamp = writeTimestamp;
    }

    public OpenMetricsSnapshotsWriter(Predicate<MetricMetadata> filterMetrics, String decimalFormat) {
        this(filterMetrics, decimalFormat, false);
    }

    public OpenMetricsSnapshotsWriter(String doubleFormat) {
        super(doubleFormat);
        writeTimestamp = false;
    }

    public OpenMetricsSnapshotsWriter() {
        super();
        writeTimestamp = false;
    }

    private String getMetricTypeName(MetricType metricType) {
        String typeName = METRIC_TYPES.get(metricType);
        if (typeName == null) {
            throw new IllegalArgumentException("Unsupported metric type: " + metricType);
        }
        return typeName;
    }

    @Override
    public void export(@NonNull MetricsSnapshot snapshot, OutputStream outputStream) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        long snapshotTimestamp = snapshot.createdTime().toEpochMilli();

        for (MetricSnapshot metricSnapshot : snapshot.snapshots()) {
            MetricMetadata metadata = metricSnapshot.metadata();

            if (!filterMetrics.test(metadata)) {
                continue;
            }

            String metricName = fix(metadata.getName());
            String metricUnit = metadata.getUnit();

            if (!metricUnit.isEmpty()) {
                metricUnit = fix(metricUnit);
                metricName += '_' + metricUnit;
            }

            writeMetadataLine(writer, "# TYPE ", metricName, getMetricTypeName(metadata.getMetricType()));
            if (!metadata.getUnit().isEmpty()) {
                writeMetadataLine(writer, "# UNIT ", metricName, metricUnit);
            }
            if (!metadata.getDescription().isEmpty()) {
                writeMetadataLine(writer, "# HELP ", metricName, metadata.getDescription());
            }

            for (DataPointSnapshot dataPoint : metricSnapshot.dataPoints()) {
                for (DataPointSnapshot.ValueItem valueItem : dataPoint.valueItems()) {
                    writer.write(metricName);
                    if (metadata.getMetricType() == MetricType.COUNTER) {
                        writer.write(COUNTER_SUFFIX);
                    }
                    if (metadata.getMetricType() == MetricType.INFO) {
                        writer.write(INFO_SUFFIX);
                    }

                    writeLabels(writer, dataPoint.labels(), valueItem.labels());
                    writer.write(' ');

                    writeDouble(writer, valueItem.value());

                    if (writeTimestamp) {
                        writer.write(' ');
                        writeOpenMetricsTimestamp(writer, snapshotTimestamp);
                    }

                    writer.write('\n');
                }
            }
        }

        writer.write("# EOF\n");
        writer.flush();
    }

    private void writeMetadataLine(Writer writer, String header, String metricName, String value) throws IOException {
        writer.write(header);
        writer.write(metricName);
        writer.write(' ');
        writeEscaped(writer, value);
        writer.write('\n');
    }

    private String fix(final String name) {
        return name.strip()
                .replace('.', ':')
                .replace('-', '_')
                .replace(' ', '_')
                .replace("/", "_per_")
                .replace("%", "Percent")
                .replaceAll("[^\\w:]", "");
    }

    private void writeLabels(Writer writer, List<Label> labels, List<Label> itemLabels) throws IOException {
        int totalLabels = labels.size() + itemLabels.size();
        if (totalLabels == 0) {
            return;
        }
        writer.write('{');
        for (int i = 0; i < totalLabels; i++) {
            if (i > 0) {
                writer.write(",");
            }

            Label label = i < labels.size() ? labels.get(i) : itemLabels.get(i - labels.size());

            writeEscaped(writer, fix(label.getName()));
            writer.write('=');
            writer.write('\"');
            writeEscaped(writer, label.getValue());
            writer.write("\"");
        }
        writer.write('}');
    }

    private void writeDouble(Writer writer, double value) throws IOException {
        if (value == Double.POSITIVE_INFINITY) {
            writer.write("+Inf");
        } else if (value == Double.NEGATIVE_INFINITY) {
            writer.write("-Inf");
        } else {
            // we cannot trust format from metadata here, because it may not be compatible with open metrics
            writer.write(formatter.format(value));
        }
    }

    private void writeEscaped(Writer writer, String str) throws IOException {
        // optimize for the common case where no escaping is needed
        int start = 0;
        // #indexOf is a vectorized intrinsic
        int backslashIndex = str.indexOf('\\', start);
        int quoteIndex = str.indexOf('\"', start);
        int newlineIndex = str.indexOf('\n', start);

        int allEscapesIndex = backslashIndex & quoteIndex & newlineIndex;
        while (allEscapesIndex != -1) {
            int escapeStart = Integer.MAX_VALUE;
            if (backslashIndex != -1) {
                escapeStart = backslashIndex;
            }
            if (quoteIndex != -1) {
                escapeStart = Math.min(escapeStart, quoteIndex);
            }
            if (newlineIndex != -1) {
                escapeStart = Math.min(escapeStart, newlineIndex);
            }

            // bulk write up to the first character that needs to be escaped
            if (escapeStart > start) {
                writer.write(str, start, escapeStart - start);
            }
            char c = str.charAt(escapeStart);
            start = escapeStart + 1;
            switch (c) {
                case '\\':
                    writer.write("\\\\");
                    backslashIndex = str.indexOf('\\', start);
                    break;
                case '\"':
                    writer.write("\\\"");
                    quoteIndex = str.indexOf('\"', start);
                    break;
                case '\n':
                    writer.write("\\n");
                    newlineIndex = str.indexOf('\n', start);
                    break;
            }

            allEscapesIndex = backslashIndex & quoteIndex & newlineIndex;
        }
        // up until the end nothing needs to be escaped anymore
        int remaining = str.length() - start;
        if (remaining > 0) {
            writer.write(str, start, remaining);
        }
    }

    private void writeOpenMetricsTimestamp(Writer writer, long timestampMs) throws IOException {
        writer.write(Long.toString(timestampMs / 1000L));
        writer.write(".");
        long ms = timestampMs % 1000;
        if (ms < 100) {
            writer.write("0");
        }
        if (ms < 10) {
            writer.write("0");
        }
        writer.write(Long.toString(ms));
    }
}
