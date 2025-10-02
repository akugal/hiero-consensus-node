// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.internal.datapoint.DataPointHolder;
import org.hiero.metrics.internal.export.SingleValueDataPointSnapshot;

public abstract class AbstractStatefulSingleValueMetric<I, D extends DoubleSupplier>
        extends AbstractStatefulMetric<I, D> {

    protected AbstractStatefulSingleValueMetric(StatefulMetric.Builder<I, D, ?, ?> builder) {
        super(builder);
    }

    @Override
    protected SingleValueDataPointSnapshot createDataPointSnapshot(LabelValues dynamicLabelValues) {
        return new SingleValueDataPointSnapshot(dynamicLabelValues);
    }

    @Override
    protected void updateDatapointSnapshot(DataPointHolder<D> dataPointHolder) {
        dataPointHolder.snapshot().setValueAt(0, dataPointHolder.dataPoint().getAsDouble());
    }
}
