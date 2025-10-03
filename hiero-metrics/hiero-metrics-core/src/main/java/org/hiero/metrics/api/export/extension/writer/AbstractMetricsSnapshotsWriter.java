// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Predicate;
import org.hiero.metrics.api.core.MetricMetadata;
import org.hiero.metrics.api.export.snapshot.MetricSnapshot;
import org.hiero.metrics.api.export.snapshot.MetricsSnapshot;

/**
 * Abstract base class for {@link MetricsSnapshotsWriter} implementations.
 * Provides common functionality such as metric filtering and number formatting.
 * <p>
 * Implementations must also extend {@link AbstractMetricsSnapshotsWriter.Builder} to add any additional configuration,
 * if needed, and pass builder to the constructor of the implementation.
 */
public abstract class AbstractMetricsSnapshotsWriter implements MetricsSnapshotsWriter {

    private final Predicate<MetricMetadata> metricFilter;
    private final DecimalFormat formatter;

    public AbstractMetricsSnapshotsWriter(Builder<?, ?> builder) {
        this.metricFilter = builder.metricFilter;
        this.formatter = builder.formatter;
    }

    protected abstract void writeMetricSnapshot(Instant timestamp, MetricSnapshot metricSnapshot, OutputStream output)
            throws IOException;

    @Override
    public final void write(@NonNull MetricsSnapshot snapshots, @NonNull OutputStream output) throws IOException {
        beforeSnapshotsWrite(snapshots, output);

        for (MetricSnapshot metricSnapshot : snapshots) {
            if (!shouldSkip(metricSnapshot.metadata())) {
                writeMetricSnapshot(snapshots.createAt(), metricSnapshot, output);
            }
        }

        afterSnapshotsWrite(snapshots, output);
    }

    protected void beforeSnapshotsWrite(@NonNull MetricsSnapshot snapshots, @NonNull OutputStream output) {
        // nothing by default
    }

    protected void afterSnapshotsWrite(@NonNull MetricsSnapshot snapshots, @NonNull OutputStream output)
            throws IOException {
        output.flush();
    }

    protected final String format(double value) {
        return formatter.format(value);
    }

    private boolean shouldSkip(MetricMetadata metadata) {
        return !metricFilter.test(metadata);
    }

    /**
     * Base builder for {@link AbstractMetricsSnapshotsWriter} implementations.<br>
     * By default, it allows all metrics and uses a default decimal format of "#.####".
     *
     * @param <B> the type of the builder subclass
     * @param <W> the type of the writer subclass
     */
    public abstract static class Builder<B extends Builder<B, W>, W extends AbstractMetricsSnapshotsWriter> {

        private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.####");
        private static final Predicate<MetricMetadata> ALLOW_ALL = metadata -> true;

        private Predicate<MetricMetadata> metricFilter = ALLOW_ALL;
        private DecimalFormat formatter = DEFAULT_DECIMAL_FORMAT;

        /**
         * Sets a filter to determine which metrics should be exported.
         * By default, all metrics are exported.
         *
         * @param metricFilter the predicate to filter metrics
         * @return the builder instance
         */
        @NonNull
        public B withMetricFilter(@NonNull Predicate<MetricMetadata> metricFilter) {
            this.metricFilter = Objects.requireNonNull(metricFilter, "metric filter cannot be null");
            return self();
        }

        /**
         * Sets the decimal format for formatting numeric metric values.
         * By default, the format is "#.####".
         *
         * @param format the decimal format
         * @return the builder instance
         */
        @NonNull
        public B withDecimalFormat(@NonNull String format) {
            this.formatter = new DecimalFormat(Objects.requireNonNull(format, "format cannot be null"));
            return self();
        }

        /**
         * Builds the {@link AbstractMetricsSnapshotsWriter} instance.
         *
         * @return the built writer instance
         */
        public abstract W build();

        /**
         * @return the builder instance to allow method chaining
         */
        @NonNull
        protected abstract B self();
    }
}
