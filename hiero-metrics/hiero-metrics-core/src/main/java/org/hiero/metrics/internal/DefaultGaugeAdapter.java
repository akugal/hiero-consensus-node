// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultGaugeAdapter<D> extends AbstractStatefulMetric<D> implements GaugeAdapter<D> {

    private final Function<D, Number> snapshotGetter;
    private final Consumer<D> reset;

    public DefaultGaugeAdapter(GaugeAdapter.Builder<D> builder) {
        super(builder);

        snapshotGetter = builder.getSnapshotGetter();
        reset = builder.getReset() != null ? builder.getReset() : container -> {};
    }

    @Override
    protected void reset(D dataPoint) {
        reset.accept(dataPoint);
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(D datapoint) {
        Number value = snapshotGetter.apply(datapoint);
        if (value == null) {
            return List.of();
        }
        return List.of(new DataPointSnapshot.ValueItem(value.doubleValue()));
    }
}
