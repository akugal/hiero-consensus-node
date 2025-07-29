// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.core.MetricRegistry;

public interface MetricsSnapshotManager {

    void manageMetricRegistry(@NonNull MetricRegistry metricRegistry);

    void resetAll();

    boolean hasRunningSnapshotThread();

    void shutdown();
}
