// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.stat;

import static org.hiero.metrics.api.stat.StatUtils.INT_AVERAGE;

import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.stat.container.AtomicIntPair;

public class AverageIntStat implements DoubleSupplier {

    private final AtomicIntPair container = AtomicIntPair.createAccumulatingSum();

    public static GaugeAdapter.Builder<AverageIntStat> metricBuilder(MetricKey<GaugeAdapter<AverageIntStat>> key) {
        return GaugeAdapter.builder(key, AverageIntStat::new, AverageIntStat::getAsDouble)
                .withReset(AverageIntStat::reset);
    }

    public static GaugeAdapter.Builder<AverageIntStat> metricBuilderReset(MetricKey<GaugeAdapter<AverageIntStat>> key) {
        return GaugeAdapter.builder(key, AverageIntStat::new, AverageIntStat::getAndReset)
                .withReset(AverageIntStat::reset);
    }

    public void add(int value) {
        container.accumulate(value, 1);
    }

    public void reset() {
        container.reset();
    }

    @Override
    public double getAsDouble() {
        return container.computeDouble(INT_AVERAGE);
    }

    public double getAndReset() {
        return container.computeDoubleAndReset(INT_AVERAGE);
    }
}
