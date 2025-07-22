// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot.extension;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.api.snapshot.MetricSnapshot;
import org.hiero.metrics.api.snapshot.MetricsSnapshot;

public class CsvMetricsSnapshotsWriter extends AbstractMetricsSnapshotsWriter {

    public static final MetricsSnapshotsWriter DEFAULT =
            new CsvMetricsSnapshotsWriter(ALLOW_ALL, DEFAULT_DECIMAL_FORMAT);

    public CsvMetricsSnapshotsWriter(@NonNull Predicate<MetricMetadata> filterMetrics, @NonNull String decimalFormat) {
        super(filterMetrics, decimalFormat);
    }

    public void writeHeaders(OutputStream outputStream) throws IOException {
        outputStream.write("timestamp,name,unit,value,labels\n".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void export(@NonNull MetricsSnapshot snapshot, OutputStream outputStream) throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

        for (MetricSnapshot metricSnapshot : snapshot.snapshots()) {
            MetricMetadata metadata = metricSnapshot.metadata();

            if (!filterMetrics.test(metadata)) {
                continue;
            }

            String timestamp = snapshot.createdTime().toString();
            String metricName = metadata.getName();

            for (DataPointSnapshot dataPoint : metricSnapshot.dataPoints()) {
                for (DataPointSnapshot.ValueItem valueItem : dataPoint.valueItems()) {
                    writer.write(timestamp); // TODO format timestamp
                    writer.write(',');

                    writer.write(metricName);
                    writer.write(',');

                    writer.write(metadata.getUnit());
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

            writer.write(label.getName());
            writer.write('=');
            writer.write(label.getValue());
        }

        writer.write('"'); // End quote
    }

    public static void main(String[] args) {
        System.out.println(Instant.now().toString());
    }
}
