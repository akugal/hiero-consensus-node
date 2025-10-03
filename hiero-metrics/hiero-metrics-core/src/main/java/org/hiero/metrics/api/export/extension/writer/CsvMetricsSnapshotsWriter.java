// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;


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
 *//*

   // TODO extend AbstractCachingMetricsSnapshotsWriter
   // TODO write metadata to separate file
   public class CsvMetricsSnapshotsWriter extends AbstractCachingMetricsSnapshotsWriter<CsvMetricsSnapshotsWriter.MetricExportData> {

       public static final CsvMetricsSnapshotsWriter DEFAULT = builder().build();

       private CsvMetricsSnapshotsWriter(Builder builder) {
           super(builder);
       }

       @NonNull
       public static Builder builder() {
           return new Builder();
       }

       public void writeHeaders(OutputStream outputStream) throws IOException {
           outputStream.write("timestamp,metric,unit,value,labels\n".getBytes(StandardCharsets.UTF_8));
       }

       @Override
       protected void writeMetricSnapshot(Instant timestamp, MetricSnapshot metricSnapshot, OutputStream output) throws IOException {
           String metricName = metricSnapshot.metadata().name();

           for (int dIdx = 0; dIdx < metricSnapshot.size(); dIdx++) {
               DataPointSnapshot dataPoint = metricSnapshot.get(dIdx);

               for (int vIdx = 0; vIdx < dataPoint.valuesSize(); vIdx++) {
                   output.write(timestamp.toString().getBytes(StandardCharsets.UTF_8));
                   output.write(',');
                   output.write(metricName.getBytes(StandardCharsets.UTF_8));
                   output.write(',');
                   output.write(metricSnapshot.metadata().unit().getBytes(StandardCharsets.UTF_8));
                   output.write(',');
                   output.write(format(dataPoint.valueAt(vIdx)).getBytes(StandardCharsets.UTF_8));
                   output.write(',');
                   writeLabels();
               }


               if (dataPoint.isSingleValue()) {
                   SingleValueDataPointSnapshot singleValueDataPoint = dataPoint.asSingleValue();

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

       public class MetricExportData extends BaseMetricExportData {

           public MetricExportData(MetricSnapshot metricSnapshot) {
               super(metricSnapshot);
           }

           @Override
           protected TemplateByteArray buildDataPointExportData(DataPointSnapshot dataPointSnapshot) {
               TemplateByteArray.Builder buffer = TemplateByteArray.builder();
               buffer.addPlaceholder(); // timestamp
               buffer.append(COMMA);
               buffer.append(metricSnapshot().metadata().name());
               buffer.append(COMMA);
               buffer.append(metricSnapshot().metadata().unit());
               buffer.append(COMMA);
               buffer.addPlaceholder(); // value
               WriterUtils.appendLabels();

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

       public static class Builder extends AbstractMetricsSnapshotsWriter.Builder<Builder, CsvMetricsSnapshotsWriter> {

           @Override
           public CsvMetricsSnapshotsWriter build() {
               return new CsvMetricsSnapshotsWriter(this);
           }

           @NonNull
           @Override
           protected Builder self() {
               return this;
           }
       }
   }
   */
