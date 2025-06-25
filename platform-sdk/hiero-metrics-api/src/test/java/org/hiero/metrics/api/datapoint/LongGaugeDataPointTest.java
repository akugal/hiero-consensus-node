// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import static org.assertj.core.api.Assertions.assertThat;

import org.hiero.metrics.api.core.StatUtils;
import org.hiero.metrics.api.datapoint.impl.AtomicLongGaugeDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongAccumulatorGaugeDataPoint;
import org.junit.jupiter.api.Test;

public class LongGaugeDataPointTest {

    @Test
    void testSumGauge() {
        LongGaugeDataPoint dataPoint = new LongAccumulatorGaugeDataPoint(StatUtils.LONG_SUM, 0);

        assertThat(dataPoint.getAsLong()).isEqualTo(0);

        dataPoint.update(5);
        dataPoint.update(10);
        assertThat(dataPoint.getAsLong()).isEqualTo(15);
        assertThat(dataPoint.getAndReset()).isEqualTo(15);
        assertThat(dataPoint.getAsLong()).isEqualTo(0);
    }

    @Test
    void testMaxGauge() {
        LongGaugeDataPoint dataPoint = new LongAccumulatorGaugeDataPoint(StatUtils.LONG_MAX, 0);

        assertThat(dataPoint.getAsLong()).isEqualTo(0);

        dataPoint.update(10);
        dataPoint.update(5);
        assertThat(dataPoint.getAsLong()).isEqualTo(10);

        dataPoint.update(15);
        assertThat(dataPoint.getAndReset()).isEqualTo(15);
        assertThat(dataPoint.getAsLong()).isEqualTo(0);
    }

    @Test
    void testMinGauge() {
        LongGaugeDataPoint dataPoint = new LongAccumulatorGaugeDataPoint(StatUtils.LONG_MIN, Long.MAX_VALUE);

        assertThat(dataPoint.getAsLong()).isEqualTo(Long.MAX_VALUE);

        dataPoint.update(5);
        dataPoint.update(10);
        assertThat(dataPoint.getAsLong()).isEqualTo(5);

        dataPoint.update(3);
        assertThat(dataPoint.getAndReset()).isEqualTo(3);
        assertThat(dataPoint.getAsLong()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void testLatestGauge() {
        LongGaugeDataPoint dataPoint = new AtomicLongGaugeDataPoint(0);

        assertThat(dataPoint.getAsLong()).isEqualTo(0);

        dataPoint.update(5);
        dataPoint.update(10);
        assertThat(dataPoint.getAsLong()).isEqualTo(10);

        dataPoint.update(3);
        assertThat(dataPoint.getAndReset()).isEqualTo(3);
        assertThat(dataPoint.getAsLong()).isEqualTo(0);
    }
}
