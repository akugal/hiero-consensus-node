// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.snapshot;

import java.util.Optional;
import java.util.function.Supplier;

public interface PullingMetricsExporter {

    String getName();

    void init(Supplier<Optional<MetricsSnapshot>> snapshotSupplier);
}
