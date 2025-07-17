// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot.extension;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.hiero.metrics.api.snapshot.MetricsSnapshot;
import org.hiero.metrics.api.snapshot.PullingMetricsExporter;

public abstract class PullingMetricsExporterSnapshotsHolder implements PullingMetricsExporter {

    private final String name;

    private final AtomicReference<Supplier<Optional<MetricsSnapshot>>> snapshotSupplierRef =
            new AtomicReference<>(Optional::empty);

    public PullingMetricsExporterSnapshotsHolder(String name) {
        this.name = ArgumentUtils.throwArgBlank(name, "exporter name");
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final void init(Supplier<Optional<MetricsSnapshot>> snapshotSupplier) {
        snapshotSupplierRef.set(snapshotSupplier);
    }

    @NonNull
    protected Optional<MetricsSnapshot> getSnapshot() {
        return snapshotSupplierRef.get().get();
    }
}
