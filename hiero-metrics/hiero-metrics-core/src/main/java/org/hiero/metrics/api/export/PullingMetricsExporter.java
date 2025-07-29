// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Optional;
import java.util.function.Supplier;

public interface PullingMetricsExporter extends Exporter {

    void init(@NonNull Supplier<Optional<MetricsSnapshot>> snapshotSupplier);
}
