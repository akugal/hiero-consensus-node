// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.hiero.metrics.api.StateSet;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.datapoint.StateSetDataPoint;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.api.utils.MetricUtils;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public class DefaultStateSet<T> extends AbstractStatefulMetric<StateSetDataPoint<T>> implements StateSet<T> {

    private final Map<T, Label> labelsCache = new HashMap<>();
    private final Function<T, Label> stateLabelFactory;

    public DefaultStateSet(StateSet.Builder<T> builder) {
        super(builder);
        stateLabelFactory = state -> new Label(getMetadata().getName(), state.toString());
    }

    @Override
    protected void reset(StateSetDataPoint<T> dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(StateSetDataPoint<T> datapoint) {
        final Set<T> states = datapoint.getStates();
        final List<DataPointSnapshot.ValueItem> items = new ArrayList<>();

        for (T state : states) {
            items.add(new DataPointSnapshot.ValueItem(
                    datapoint.getState(state) ? MetricUtils.ONE : MetricUtils.ZERO,
                    labelsCache.computeIfAbsent(state, stateLabelFactory)));
        }

        return items;
    }
}
