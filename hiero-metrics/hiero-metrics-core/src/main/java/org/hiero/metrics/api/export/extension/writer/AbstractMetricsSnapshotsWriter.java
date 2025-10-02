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
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.api.export.MetricsSnapshot;

/**
 * Abstract base class for {@link MetricsSnapshotsWriter} implementations.
 * Provides common functionality such as metric filtering and number formatting.
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

    public abstract static class Builder<B extends Builder<B, W>, W extends AbstractMetricsSnapshotsWriter> {

        private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("#.####");
        private static final Predicate<MetricMetadata> ALLOW_ALL = metadata -> true;

        private Predicate<MetricMetadata> metricFilter = ALLOW_ALL;
        private DecimalFormat formatter = DEFAULT_DECIMAL_FORMAT;

        @NonNull
        public B withMetricFilter(@NonNull Predicate<MetricMetadata> metricFilter) {
            this.metricFilter = Objects.requireNonNull(metricFilter, "metric filter cannot be null");
            return self();
        }

        @NonNull
        public B withDecimalFormat(@NonNull String format) {
            this.formatter = new DecimalFormat(Objects.requireNonNull(format, "format cannot be null"));
            return self();
        }

        public abstract W build();

        @NonNull
        protected abstract B self();
    }
}
