package org.hiero.metrics.api.export;

import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.core.MetricSnapshot;
import org.hiero.metrics.api.core.MetricType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;

public class OpenMetricsExporter extends AbstractMetricsExporter {

    private static final String COUNTER_SUFFIX = "_total";

    public OpenMetricsExporter(Predicate<MetricMetadata> filterMetrics, String decimalFormat) {
        super(filterMetrics, decimalFormat);
    }

    public OpenMetricsExporter(String doubleFormat) {
        super(doubleFormat);
    }

    public OpenMetricsExporter() {
        super();
    }

    @Override
    public void export(List<MetricSnapshot> snapshots, OutputStream outputStream) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        for (MetricSnapshot snapshot : snapshots) {
            MetricMetadata metadata = snapshot.metadata();

            if (!filterMetrics.test(metadata)) {
                continue;
            }

            String metricName = fix(metadata.getFullName());
            String metricUnit = metadata.getUnit();

            if (!metricUnit.isEmpty()) {
                metricUnit = fix(metricUnit);
                metricName += '_' + metricUnit;
            }

            writeMetadataLine(writer, "# TYPE ", metricName, metadata.getMetricType().getName());
            if (!metadata.getUnit().isEmpty()) {
                writeMetadataLine(writer, "# UNIT ", metricName, metricUnit);
            }
            if (!metadata.getDescription().isEmpty()) {
                writeMetadataLine(writer, "# HELP ", metricName, metadata.getDescription());
            }

            for (DataPointSnapshot dataPoint : snapshot.dataPoints()) {
                writer.write(metricName);
                if (dataPoint.classifier() != null) {
                    writer.write('_');
                    writer.write(fix(dataPoint.classifier()));
                }

                if (metadata.getMetricType() == MetricType.COUNTER) {
                    writer.write(COUNTER_SUFFIX);
                }

                writer.write(' ');
                if (!dataPoint.labels().isEmpty()) {
                    writeLabels(writer, dataPoint.labels());
                    writer.write(' ');
                }

                writeDouble(writer, dataPoint.value());
                writer.write('\n');
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

    private void writeLabels(Writer writer, List<Label> labels) throws IOException {
        writer.write('{');
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) {
                writer.write(",");
            }

            Label label = labels.get(i);

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
