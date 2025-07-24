// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.snapshot;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.snapshot.MetricsSnapshotManager;

public class NoOpMetricsSnapshotManager implements MetricsSnapshotManager {

    public static final MetricsSnapshotManager INSTANCE = new NoOpMetricsSnapshotManager();

    @Override
    public void manageMetricRegistry(@NonNull MetricRegistry metricRegistry) {
        // no op
    }

    @Override
    public void resetAll() {
        // no op
    }

    @Override
    public boolean hasRunningSnapshotThread() {
        return false;
    }

    @Override
    public void shutdown() {
        // no op
    }
}
