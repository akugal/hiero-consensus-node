// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.core.MetricCallback;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractMetric;
import org.hiero.metrics.internal.core.SnapshotableMetric;

public final class DefaultCallbackMetric extends AbstractMetric implements CallbackMetric, SnapshotableMetric {

    private final Consumer<MetricCallback> callback;

    public DefaultCallbackMetric(CallbackMetric.Builder builder) {
        super(builder);
        callback = builder.getCallback();
    }

    @NonNull
    @Override
    public List<DataPointSnapshot> snapshot() {
        List<DataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept((value, labelValues) ->
                dataPoints.add(new DataPointSnapshot(createDataPointLabels(Arrays.asList(labelValues)), value)));
        return dataPoints;
    }
}
