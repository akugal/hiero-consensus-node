// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot.extension;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.snapshot.MetricsSnapshot;
import org.hiero.metrics.api.snapshot.PullingMetricsExporter;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class PullingMetricsExporterAdapter implements PullingMetricsExporter {

    private final String name;

    private volatile Supplier<Optional<MetricsSnapshot>> snapshotSupplier = Optional::empty;

    public PullingMetricsExporterAdapter(String name) {
        this.name = ArgumentUtils.throwArgBlank(name, "exporter name");
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final void init(@NonNull Supplier<Optional<MetricsSnapshot>> snapshotSupplier) {
        this.snapshotSupplier = Objects.requireNonNull(snapshotSupplier);
    }

    @NonNull
    public Optional<MetricsSnapshot> getSnapshot() {
        return snapshotSupplier.get();
    }
}
