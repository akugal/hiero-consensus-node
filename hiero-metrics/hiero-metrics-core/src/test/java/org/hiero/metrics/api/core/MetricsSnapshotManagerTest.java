// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hiero.metrics.api.stat.StatUtils.DEFAULT_STAT_LABEL;

import java.util.List;
import org.hiero.metrics.api.DoubleGaugeComposite;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.api.export.MetricSnapshot;
import org.hiero.metrics.api.export.MetricsSnapshotManager;
import org.hiero.metrics.api.export.extension.PullingMetricsExporterAdapter;
import org.junit.jupiter.api.Test;

public class MetricsSnapshotManagerTest {

    @Test
    public void test() {
        PullingMetricsExporterAdapter exporter = new PullingMetricsExporterAdapter("test");

        MetricsSnapshotManager manager = MetricsFacade.createSnapshotManager(exporter);
        Label globalLabel = new Label("env", "test");
        MetricRegistry registry = MetricsFacade.createRegistry(globalLabel);
        manager.manageMetricRegistry(registry);

        // given
        DoubleGaugeComposite singleSumDoubleMetric = DoubleGaugeComposite.builder(
                        DoubleGaugeComposite.key("singleSumDoubleMetric"))
                .withSumStat()
                .register(registry);
        DoubleGaugeComposite multipleDoubleStats = DoubleGaugeComposite.builder(
                        DoubleGaugeComposite.key("multipleDoubleStats"))
                .withSumStat()
                .withAccumulatorStat("sumPlusOne", (v1, v2) -> v1 + v2 + 1)
                .register(registry);

        // given
        singleSumDoubleMetric.getNotLabeled().update(1.3);
        multipleDoubleStats.getNotLabeled().update(1.3);

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
        singleSumDoubleMetric.getNotLabeled().update(1.7);
        multipleDoubleStats.getNotLabeled().update(1.7);

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
