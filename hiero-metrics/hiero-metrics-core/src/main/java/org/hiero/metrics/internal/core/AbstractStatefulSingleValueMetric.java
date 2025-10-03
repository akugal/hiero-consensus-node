// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.internal.datapoint.DataPointHolder;
import org.hiero.metrics.internal.export.snapshot.DefaultSingleValueDataPointSnapshot;

public abstract class AbstractStatefulSingleValueMetric<I, D extends DoubleSupplier>
        extends AbstractStatefulMetric<I, D, DefaultSingleValueDataPointSnapshot> {

    protected AbstractStatefulSingleValueMetric(StatefulMetric.Builder<I, D, ?, ?> builder) {
        super(builder);
    }

    @Override
    protected DefaultSingleValueDataPointSnapshot createDataPointSnapshot(LabelValues dynamicLabelValues) {
        return new DefaultSingleValueDataPointSnapshot(dynamicLabelValues);
    }

    @Override
    protected void updateDatapointSnapshot(DataPointHolder<D, DefaultSingleValueDataPointSnapshot> dataPointHolder) {
        dataPointHolder.snapshot().update(dataPointHolder.dataPoint().getAsDouble());
    }
}
