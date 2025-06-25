// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.datapoint;

import static org.assertj.core.api.Assertions.assertThat;

import org.hiero.metrics.api.datapoint.impl.AtomicLongCounterDataPoint;
import org.hiero.metrics.api.datapoint.impl.LongAdderCounterDataPoint;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class LongCounterDataPointTest {

    @ParameterizedTest
    @MethodSource("dataPointImplementations")
    void testIncrement(LongCounterDataPoint dataPoint) {
        assertThat(dataPoint.getAsLong()).isEqualTo(0L);

        dataPoint.increment();
        assertThat(dataPoint.getAsLong()).isEqualTo(1L);

        dataPoint.increment(5);
        dataPoint.increment(3);
        assertThat(dataPoint.getAsLong()).isEqualTo(9L);
    }

    private static LongCounterDataPoint[] dataPointImplementations() {
        return new LongCounterDataPoint[] {new AtomicLongCounterDataPoint(), new LongAdderCounterDataPoint()};
    }
}
