// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.export.MetricsExportManager;

public class NoOpMetricsExportManager implements MetricsExportManager {

    public static final MetricsExportManager INSTANCE = new NoOpMetricsExportManager();

    @Override
    public void manageMetricRegistry(@NonNull MetricRegistry metricRegistry) {
        // no op
    }

    @Override
    public void resetAll() {
        // no op
    }

    @Override
    public boolean hasRunningExportThread() {
        return false;
    }

    @Override
    public void shutdown() {
        // no op
    }
}
