// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;

public interface SnapshotableMetric extends Metric {

    @NonNull
    List<DataPointSnapshot> snapshot();
}
