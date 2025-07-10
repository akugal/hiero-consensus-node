package org.hiero.metrics.api;

import org.hiero.metrics.api.core.DataPointSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DoubleGaugeCompositeTest {

    @Test
    public void testFailBuildWhenNoStatsDefined() {
        assertThatThrownBy(() -> DoubleGaugeComposite.builder("noStats").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("At least one stat must be defined");
    }

    @Test
    public void testFailBuildWhenDuplicateStatsDefined() {
        assertThatThrownBy(() -> DoubleGaugeComposite.builder("duplicateStat")
                .withAccumulatorStat("stat1", Double::max, 0.0)
                .withAccumulatorStat("stat1", Double::min, 0.0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stat names must be unique");
    }

    @Test
    public void testSingleSumStat() {
        DoubleGaugeComposite metric = DoubleGaugeComposite.builder("singleSumStat")
                .withSumStat()
                .build();

        assertThat(metric.size()).isEqualTo(1);

        // given
        metric.update(1.3);

        // than
        assertThat(metric.get(0).getAsDouble()).isEqualTo(1.3);

        List<DataPointSnapshot> snapshot = metric.snapshot();
        assertThat(snapshot.size()).isEqualTo(1);
        assertThat(snapshot.get(0)).isEqualTo(new DataPointSnapshot("sum", 1.3, List.of()));

        // given
        metric.update(1.7);

        // than
        assertThat(metric.get(0).getAsDouble()).isEqualTo(3.0);

        snapshot = metric.snapshot();
        assertThat(snapshot.size()).isEqualTo(1);
        assertThat(snapshot.get(0)).isEqualTo(new DataPointSnapshot("sum", 3.0, List.of()));
    }

    // TODO other stats tests


    @Test
    public void testMultipleStats() {
        DoubleGaugeComposite metric = DoubleGaugeComposite.builder("multipleStats")
                .withSumStat()
                .withAccumulatorStat("sumPlusOne", (v1, v2) -> v1 + v2 + 1)
                .build();

        assertThat(metric.size()).isEqualTo(2);

        // given
        metric.update(1.3);

        // than
        assertThat(metric.get(0).getAsDouble()).isEqualTo(1.3);
        assertThat(metric.get(1).getAsDouble()).isEqualTo(2.3);

        List<DataPointSnapshot> snapshot = metric.snapshot();
        assertThat(snapshot.size()).isEqualTo(2);
        assertThat(snapshot.get(0)).isEqualTo(new DataPointSnapshot("sum", 1.3, List.of()));
        assertThat(snapshot.get(1)).isEqualTo(new DataPointSnapshot("sumPlusOne", 2.3, List.of()));

        // given
        metric.update(1.7);

        // than
        assertThat(metric.get(0).getAsDouble()).isEqualTo(3.0);
        assertThat(metric.get(1).getAsDouble()).isEqualTo(5.0);

        snapshot = metric.snapshot();
        assertThat(snapshot.size()).isEqualTo(2);
        assertThat(snapshot.get(0)).isEqualTo(new DataPointSnapshot("sum", 3.0, List.of()));
        assertThat(snapshot.get(1)).isEqualTo(new DataPointSnapshot("sumPlusOne", 5.0, List.of()));
    }
}
