// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.hiero.metrics.api.StatsGaugeAdapter;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultStatsGaugeAdapter<D> extends AbstractStatefulMetric<D> implements StatsGaugeAdapter<D> {

    private final Label[] statLabels;
    private final Function<D, Number>[] statSnapshotGetters;
    private final Consumer<D> reset;

    @SuppressWarnings("unchecked")
    public DefaultStatsGaugeAdapter(StatsGaugeAdapter.Builder<D> builder) {
        super(builder);

        reset = builder.getReset() != null ? builder.getReset() : container -> {}; // no-op reset if no specified
        statSnapshotGetters = builder.getStatSnapshotGetters().toArray(new Function[0]);
        statLabels = new Label[builder.getStatNames().size()];
        for (int i = 0; i < statLabels.length; i++) {
            statLabels[i] =
                    new Label(builder.getStatLabel(), builder.getStatNames().get(i));
        }
    }

    @Override
    protected void reset(D dataPoint) {
        reset.accept(dataPoint);
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(D datapoint) {
        List<DataPointSnapshot.ValueItem> valueItems = new ArrayList<>(statSnapshotGetters.length);
        for (int i = 0; i < statSnapshotGetters.length; i++) {
            Number value = statSnapshotGetters[i].apply(datapoint);
            if (value != null) {
                valueItems.add(new DataPointSnapshot.ValueItem(value.doubleValue(), statLabels[i]));
            }
        }

        return valueItems;
    }
}
