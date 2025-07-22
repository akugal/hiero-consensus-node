// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import org.hiero.metrics.api.DoubleGaugeComposite;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.datapoint.DoubleGaugeCompositeDataPoint;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.internal.core.AbstractStatefulMetric;

public final class DefaultDoubleGaugeComposite extends AbstractStatefulMetric<DoubleGaugeCompositeDataPoint>
        implements DoubleGaugeComposite {

    private final Label[] statLabels;
    private final ToDoubleFunction<DoubleGaugeDataPoint> snapshotValueSupplier;

    public DefaultDoubleGaugeComposite(DoubleGaugeComposite.Builder builder) {
        super(builder);

        snapshotValueSupplier =
                builder.isResetOnSnapshot() ? DoubleGaugeDataPoint::getAndReset : DoubleGaugeDataPoint::getAsDouble;

        statLabels = new Label[builder.getStatNames().size()];
        for (int i = 0; i < statLabels.length; i++) {
            statLabels[i] =
                    new Label(builder.getStatLabel(), builder.getStatNames().get(i));
        }
    }

    @Override
    public void reset(DoubleGaugeCompositeDataPoint dataPoint) {
        dataPoint.reset();
    }

    @NonNull
    @Override
    protected List<DataPointSnapshot.ValueItem> snapshotDataPoint(DoubleGaugeCompositeDataPoint datapoint) {
        List<DataPointSnapshot.ValueItem> valueItems = new ArrayList<>(datapoint.size());
        for (int i = 0; i < datapoint.size(); i++) {
            double value = snapshotValueSupplier.applyAsDouble(datapoint.get(i));
            if (Double.MAX_VALUE != value && Double.MIN_VALUE != value) {
                valueItems.add(new DataPointSnapshot.ValueItem(value, statLabels[i]));
            }
        }
        return valueItems;
    }

    @Override
    public void update(double value) {
        getNoLabels().update(value);
    }

    @Override
    public int size() {
        return getNoLabels().size();
    }

    @Override
    public DoubleGaugeDataPoint get(int index) {
        return getNoLabels().get(index);
    }
}
