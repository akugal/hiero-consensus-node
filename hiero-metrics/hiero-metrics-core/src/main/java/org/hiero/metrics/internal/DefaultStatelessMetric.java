// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.StatelessMetric;
import org.hiero.metrics.internal.core.AbstractMetric;
import org.hiero.metrics.internal.core.LabelValues;
import org.hiero.metrics.internal.datapoint.DataPointHolder;
import org.hiero.metrics.internal.export.BaseDataPointSnapshot;
import org.hiero.metrics.internal.export.SingleValueDataPointSnapshot;

public final class DefaultStatelessMetric extends AbstractMetric<DoubleSupplier> implements StatelessMetric {

    public DefaultStatelessMetric(StatelessMetric.Builder builder) {
        super(builder);

        int dataPointsSize = builder.getDataPointsSize();
        for (int i = 0; i < dataPointsSize; i++) {
            registerDataPoint(builder.getValuesSupplier(i), builder.getDataPointsLabelNamesAndValues(i));
        }
    }

    @Override
    protected BaseDataPointSnapshot createDataPointSnapshot(LabelValues dynamicLabelValues) {
        return new SingleValueDataPointSnapshot(dynamicLabelValues);
    }

    @Override
    protected void updateDatapointSnapshot(DataPointHolder<DoubleSupplier> dataPointHolder) {
        dataPointHolder.snapshot().setValueAt(0, dataPointHolder.dataPoint().getAsDouble());
    }

    @NonNull
    @Override
    public StatelessMetric registerDataPoint(
            @NonNull DoubleSupplier valueSupplier, @NonNull String... labelNamesAndValues) {
        Objects.requireNonNull(valueSupplier, "Value supplier must not be null");

        LabelValues labelValues = createLabelValues(labelNamesAndValues);
        DataPointHolder<DoubleSupplier> dataPointHolder = createDataPointHolder(valueSupplier, labelValues);
        if (dataPoints.putIfAbsent(labelValues, dataPointHolder) != null) {
            throw new IllegalArgumentException(
                    "A data point with the same label values already exists: " + labelValues);
        }

        return this;
    }
}
