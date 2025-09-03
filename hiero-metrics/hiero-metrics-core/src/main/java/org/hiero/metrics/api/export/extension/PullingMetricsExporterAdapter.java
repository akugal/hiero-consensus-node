// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.hiero.metrics.api.export.MetricsSnapshot;
import org.hiero.metrics.api.export.PullingMetricsExporter;

/**
 * An abstract base class for {@link PullingMetricsExporter} implementations.
 * It provides a mechanism to supply metrics snapshots via a {@link Supplier}.
 */
public class PullingMetricsExporterAdapter implements PullingMetricsExporter {

    private final String name;

    private volatile Supplier<Optional<MetricsSnapshot>> snapshotSupplier = Optional::empty;

    public PullingMetricsExporterAdapter(String name) {
        this.name = ArgumentUtils.throwArgBlank(name, "exporter name");
    }

    @NonNull
    @Override
    public final String name() {
        return name;
    }

    @Override
    public final void init(@NonNull Supplier<Optional<MetricsSnapshot>> snapshotSupplier) {
        this.snapshotSupplier = Objects.requireNonNull(snapshotSupplier);
    }

    @NonNull
    public final Optional<MetricsSnapshot> getSnapshot() {
        return snapshotSupplier.get();
    }

    @Override
    public void close() throws IOException {
        snapshotSupplier = Optional::empty;
    }
}
