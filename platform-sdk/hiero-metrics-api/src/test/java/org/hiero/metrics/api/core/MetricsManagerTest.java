// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import org.hiero.metrics.api.DoubleGaugeComposite;
import org.hiero.metrics.api.snapshot.DataPointSnapshot;
import org.hiero.metrics.api.snapshot.MetricSnapshot;
import org.hiero.metrics.api.snapshot.extension.PullingMetricsExporterAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hiero.metrics.api.utils.StatUtils.DEFAULT_STAT_LABEL;

public class MetricsManagerTest {

    @Test
    public void test() {
        PullingMetricsExporterAdapter exporter = new PullingMetricsExporterAdapter("test");

        MetricsManager manager = MetricsManager.createSimple(exporter);
        Label globalLabel = new Label("env", "test");
        MetricRegistry registry = manager.createManagedMetricsRegistry("test_registry", globalLabel);

        // given
        DoubleGaugeComposite singleSumDoubleMetric = DoubleGaugeComposite.builder("singleSumDoubleMetric")
                .withSumStat()
                .register(registry);
        DoubleGaugeComposite multipleDoubleStats = DoubleGaugeComposite.builder("multipleDoubleStats")
                .withSumStat()
                .withAccumulatorStat("sumPlusOne", (v1, v2) -> v1 + v2 + 1)
                .register(registry);

        // given
        singleSumDoubleMetric.update(1.3);
        multipleDoubleStats.update(1.3);

        // then
        List<MetricSnapshot> snapshots = exporter.getSnapshot().get().snapshots();
        assertThat(snapshots)
                .containsExactlyInAnyOrder(
                        new MetricSnapshot(
                                singleSumDoubleMetric.getMetadata(),
                                List.of(new DataPointSnapshot(
                                        List.of(globalLabel),
                                        new DataPointSnapshot.ValueItem(1.3, new Label(DEFAULT_STAT_LABEL, "sum"))))),
                        new MetricSnapshot(
                                multipleDoubleStats.getMetadata(),
                                List.of(new DataPointSnapshot(
                                        List.of(globalLabel),
                                        new DataPointSnapshot.ValueItem(1.3, new Label(DEFAULT_STAT_LABEL, "sum")),
                                        new DataPointSnapshot.ValueItem(
                                                2.3, new Label(DEFAULT_STAT_LABEL, "sumPlusOne"))))));

        // given
        singleSumDoubleMetric.update(1.7);
        multipleDoubleStats.update(1.7);

        // then
        snapshots = exporter.getSnapshot().get().snapshots();
        assertThat(snapshots)
                .containsExactlyInAnyOrder(
                        new MetricSnapshot(
                                singleSumDoubleMetric.getMetadata(),
                                List.of(new DataPointSnapshot(
                                        List.of(globalLabel),
                                        new DataPointSnapshot.ValueItem(3.0, new Label(DEFAULT_STAT_LABEL, "sum"))))),
                        new MetricSnapshot(
                                multipleDoubleStats.getMetadata(),
                                List.of(new DataPointSnapshot(
                                        List.of(globalLabel),
                                        new DataPointSnapshot.ValueItem(3.0, new Label(DEFAULT_STAT_LABEL, "sum")),
                                        new DataPointSnapshot.ValueItem(
                                                5.0, new Label(DEFAULT_STAT_LABEL, "sumPlusOne"))))));

        manager.shutdown();
    }
}
