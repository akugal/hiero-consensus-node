// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.core.MetricRegistry;

public interface MetricsExportManager {

    void manageMetricRegistry(@NonNull MetricRegistry metricRegistry);

    void resetAll();

    boolean hasRunningExportThread();

    void shutdown();
}
