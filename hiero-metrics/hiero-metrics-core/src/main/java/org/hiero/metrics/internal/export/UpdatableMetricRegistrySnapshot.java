// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.hiero.metrics.api.core.ArrayAccessor;
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.internal.core.AppendArray;

public final class UpdatableMetricRegistrySnapshot implements ArrayAccessor<MetricSnapshot> {

    private final AppendArray<UpdatableMetricSnapshot<?>> snapshots = new AppendArray<>(64);

    public void updateSnapshot() {
        snapshots.readyToRead(UpdatableMetricSnapshot::updateSnapshot);
    }

    public void add(SnapshotableMetric snapshotableMetric) {
        snapshots.add(snapshotableMetric.snapshot());
    }

    @Override
    public int size() {
        return snapshots.size();
    }

    @NonNull
    @Override
    public MetricSnapshot get(int index) {
        return snapshots.get(index);
    }
}
