// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.DoubleGaugeComposite;
import org.hiero.metrics.api.datapoint.DoubleGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;
import org.hiero.metrics.internal.core.LabelValues;
import org.hiero.metrics.internal.datapoint.DataPointHolder;
import org.hiero.metrics.internal.export.FixedMultiValueDataPointSnapshot;

public final class DefaultDoubleGaugeComposite extends AbstractStatefulMetric<Object, DoubleGaugeCompositeDataPoint>
        implements DoubleGaugeComposite {

    private final String statLabelName;
    private final String[] statLabelValues;
    private final ToDoubleFunction<DoubleGaugeDataPoint> exportValueSupplier;

    public DefaultDoubleGaugeComposite(DoubleGaugeComposite.Builder builder) {
        super(builder);

        exportValueSupplier =
                builder.isResetOnExport() ? DoubleGaugeDataPoint::getAndReset : DoubleGaugeDataPoint::getAsDouble;

        statLabelName = builder.getStatLabel();
        statLabelValues = builder.getStatNames().toArray(new String[0]);
    }

    @Override
    protected FixedMultiValueDataPointSnapshot createDataPointSnapshot(LabelValues dynamicLabelValues) {
        return new FixedMultiValueDataPointSnapshot(dynamicLabelValues, statLabelName, statLabelValues);
    }

    @Override
    protected void updateDatapointSnapshot(DataPointHolder<DoubleGaugeCompositeDataPoint> dataPointHolder) {
        DoubleGaugeCompositeDataPoint dataPoint = dataPointHolder.dataPoint();
        for (int i = 0; i < dataPoint.size(); i++) {
            double value = exportValueSupplier.applyAsDouble(dataPoint.get(i));
            dataPointHolder.snapshot().setValueAt(i, value);
        }
    }

    @Override
    public void reset(DoubleGaugeCompositeDataPoint dataPoint) {
        dataPoint.reset();
    }
}
