// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.api.export.MetricsSnapshot;

/**
 * A {@link MetricsSnapshotsWriter} implementation that writes metrics in CSV format.
 *
 * <p>CSV Format:
 *
 * <pre>
 * timestamp,metric,unit,value,labels
 * 2024-10-01T12:00:00Z,cpu_usage,percentage,75.5,"host=server1;region=us-west"
 * 2024-10-01T12:00:00Z,memory_usage,MB,2048,"host=server1;region=us-west"
 * </pre>
 *
 * <p>Labels are enclosed in quotes and separated by semicolons to handle commas in label values.
 */
public class CsvMetricsSnapshotsWriter extends AbstractMetricsSnapshotsWriter {

    public static final CsvMetricsSnapshotsWriter DEFAULT =
            new CsvMetricsSnapshotsWriter(ALLOW_ALL, DEFAULT_DECIMAL_FORMAT);

    public CsvMetricsSnapshotsWriter(@NonNull Predicate<MetricMetadata> filterMetrics, @NonNull String decimalFormat) {
        super(filterMetrics, decimalFormat);
    }

    public void writeHeaders(OutputStream outputStream) throws IOException {
        outputStream.write("timestamp,metric,unit,value,labels\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void write(@NonNull MetricsSnapshot snapshot, OutputStream outputStream) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        for (MetricSnapshot metricSnapshot : snapshot.snapshots()) {
            MetricMetadata metadata = metricSnapshot.metadata();

            if (!filterMetrics.test(metadata)) {
                continue;
            }

            String timestamp = snapshot.createdTime().toString();
            String metricName = metadata.name();

            for (DataPointSnapshot dataPoint : metricSnapshot.dataPoints()) {
                for (DataPointSnapshot.ValueItem valueItem : dataPoint.valueItems()) {
                    writer.write(timestamp); // TODO format timestamp
                    writer.write(',');

                    writer.write(metricName);
                    writer.write(',');

                    writer.write(metadata.unit());
                    writer.write(',');

                    writer.write(formatter.format(valueItem.value()));
                    writer.write(',');

                    writeLabels(writer, dataPoint.labels(), valueItem.labels());

                    writer.write('\n');
                }
            }
        }

        writer.flush(); // Important: flush the buffer
    }

    private void writeLabels(Writer writer, List<Label> labels, List<Label> itemLabels) throws IOException {
        int totalLabels = labels.size() + itemLabels.size();
        if (totalLabels == 0) {
            // Empty quoted string for consistency
            writer.write('"');
            writer.write('"');
            return;
        }

        writer.write('"'); // Start quote

        for (int i = 0; i < totalLabels; i++) {
            if (i > 0) {
                writer.write(';'); // Use semicolon separator
            }

            Label label = i < labels.size() ? labels.get(i) : itemLabels.get(i - labels.size());

            writer.write(label.name());
            writer.write('=');
            writer.write(label.value());
        }

        writer.write('"'); // End quote
    }
}
