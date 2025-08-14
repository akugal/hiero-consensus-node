// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.stat;

import com.swirlds.base.time.Time;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.datapoint.DoubleGaugeDataPoint;

// similar to com.swirlds.common.metrics.statistics.StatsRunningAverage
public class MovingAverageStat implements DoubleGaugeDataPoint {
    /**
     * each recordValue(X) counts as X calls to values.cycle()
     */
    private final FrequencyMovingAvg values;

    /**
     * each recordValue(X) counts as 1 call to times.cycle()
     */
    private final FrequencyMovingAvg times;

    /**
     * the estimated running average
     */
    private volatile double mean = 0;

    /**
     * Did we just perform a reset, and are about to record the first value?
     */
    private boolean firstRecord;

    public MovingAverageStat(final double halfLife, Time time) {
        firstRecord = true;
        values = new FrequencyMovingAvg(halfLife, time);
        times = new FrequencyMovingAvg(halfLife, time);
        reset();
    }

    public static MetricKey<GaugeAdapter<DoubleSupplier, MovingAverageStat>> key(String name) {
        return MetricKey.of(name, GaugeAdapter.class);
    }

    public static GaugeAdapter.Builder<DoubleSupplier, MovingAverageStat> metricBuilder(
            double halfLife, Time time, MetricKey<GaugeAdapter<DoubleSupplier, MovingAverageStat>> key) {
        return GaugeAdapter.builder(
                        key,
                        StatUtils.asInitializer(halfLife),
                        init -> new MovingAverageStat(init.getAsDouble(), time),
                        MovingAverageStat::getAsDouble)
                .withReset(MovingAverageStat::reset);
    }

    public static GaugeAdapter.Builder<DoubleSupplier, MovingAverageStat> metricBuilder(
            double halfLife, MetricKey<GaugeAdapter<DoubleSupplier, MovingAverageStat>> key) {
        return metricBuilder(halfLife, Time.getCurrent(), key);
    }

    @Override
    public double getInitValue() {
        return 0;
    }

    @Override
    public synchronized void update(double value) {
        if (Double.isNaN(value)) { // java getSystemCpuLoad returns NaN at beginning
            return;
        }
        // StatsRunningAverage is not thread safe, despite this, it is accessed by many threads throughout the platform
        // Until we do a full statistics refactor, this try catch is a safeguard against any issues that might occur
        // from this issue
        values.update(value);
        times.update();

        if (firstRecord || value == mean) {
            // if the same value is always given since the beginning, then avoid roundoff errors
            firstRecord = false;
            mean = value;
        } else {
            mean = values.getAsDouble() / times.getAsDouble();
        }
    }

    @Override
    public double getAsDouble() {
        values.update(StatUtils.ZERO);
        times.update(StatUtils.ZERO);
        return mean;
    }

    @Override
    public double getAndReset() {
        final double result = mean;
        reset();
        return result;
    }

    /**
     * Start over on the measurements and counts, to get an exponentially-weighted average number of calls
     * to cycle() per second, with the weighting having a half life of halfLife seconds. This is equivalent
     * to instantiating a new instance of the class. If halfLife &lt; 0.01 then 0.01 will be used.
     */
    @Override
    public synchronized void reset() {
        firstRecord = true;
        values.reset();
        times.reset();
    }
}
