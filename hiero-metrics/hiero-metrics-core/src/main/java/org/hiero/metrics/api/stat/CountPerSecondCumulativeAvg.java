// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.stat;

import static org.hiero.metrics.api.stat.StatUtils.INT_NO_OP;
import static org.hiero.metrics.api.stat.StatUtils.INT_SUM;

import com.swirlds.base.time.Time;
import com.swirlds.base.units.UnitConstants;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleBiFunction;
import org.hiero.metrics.api.GaugeAdapter;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.stat.container.AtomicIntPair;

public class CountPerSecondCumulativeAvg implements DoubleSupplier {

    private final AtomicIntPair container = new AtomicIntPair(INT_NO_OP, INT_SUM);
    private final Time time;
    private final ToDoubleBiFunction<Integer, Integer> compute;

    public CountPerSecondCumulativeAvg(Time time) {
        this.time = time;

        compute = (startTime, count) -> {
            int millisElapsed = currentTimeMillis() - startTime;
            if (millisElapsed == 0) {
                // theoretically this is infinity, but we will say that 1 millisecond of time passed because some time
                // has to have passed
                millisElapsed = 1;
            }
            return count / (millisElapsed * UnitConstants.MILLISECONDS_TO_SECONDS);
        };
    }

    public static GaugeAdapter.Builder<CountPerSecondCumulativeAvg> metricBuilder(
            Time time, MetricKey<GaugeAdapter<CountPerSecondCumulativeAvg>> key) {
        return GaugeAdapter.builder(
                        key, () -> new CountPerSecondCumulativeAvg(time), CountPerSecondCumulativeAvg::getAndReset)
                .withReset(CountPerSecondCumulativeAvg::reset);
    }

    public static GaugeAdapter.Builder<CountPerSecondCumulativeAvg> metricBuilder(
            MetricKey<GaugeAdapter<CountPerSecondCumulativeAvg>> key) {
        return metricBuilder(Time.getCurrent(), key);
    }

    public CountPerSecondCumulativeAvg() {
        this(Time.getCurrent());
    }

    public void count() {
        count(1);
    }

    /**
     * Increase the count by the value provided
     *
     * @param count the amount to increase the count by
     */
    public void count(final int count) {
        container.accumulate(0, count);
    }

    @Override
    public double getAsDouble() {
        return container.computeDouble(compute);
    }

    public double getAndReset() {
        return container.computeDoubleAndReset(compute);
    }

    public void reset() {
        container.set(currentTimeMillis(), 0);
    }

    public int currentTimeMillis() {
        return (int) (time.currentTimeMillis() % Integer.MAX_VALUE);
    }
}
