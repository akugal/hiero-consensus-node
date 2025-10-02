// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import static org.hiero.metrics.api.stat.StatUtils.ONE;
import static org.hiero.metrics.api.stat.StatUtils.ZERO;

import java.util.List;
import org.hiero.metrics.api.StateSet;
import org.hiero.metrics.api.datapoint.StateSetDataPoint;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;
import org.hiero.metrics.internal.core.LabelValues;
import org.hiero.metrics.internal.datapoint.DataPointHolder;
import org.hiero.metrics.internal.export.FixedMultiValueDataPointSnapshot;

public class DefaultStateSet<E extends Enum<E>> extends AbstractStatefulMetric<List<E>, StateSetDataPoint<E>>
        implements StateSet<E> {

    private final E[] enumConstants;
    private final String[] valueTypes;

    public DefaultStateSet(StateSet.Builder<E> builder) {
        super(builder);

        enumConstants = builder.getEnumClass().getEnumConstants();
        valueTypes = new String[enumConstants.length];
        for (E enumConstant : enumConstants) {
            valueTypes[enumConstant.ordinal()] = enumConstant.toString();
        }
    }

    @Override
    protected FixedMultiValueDataPointSnapshot createDataPointSnapshot(LabelValues dynamicLabelValues) {
        return new FixedMultiValueDataPointSnapshot(
                dynamicLabelValues, metadata().name(), valueTypes);
    }

    @Override
    protected void updateDatapointSnapshot(DataPointHolder<StateSetDataPoint<E>> dataPointHolder) {
        for (E enumConstant : enumConstants) {
            double value = dataPointHolder.dataPoint().getState(enumConstant) ? ONE : ZERO;
            dataPointHolder.snapshot().setValueAt(enumConstant.ordinal(), value);
        }
    }

    @Override
    protected void reset(StateSetDataPoint<E> dataPoint) {
        dataPoint.reset();
    }
}
