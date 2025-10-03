// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

/*public class MetricsExportManagerTest {

    @Test
    public void test() {
        PullingMetricsExporterAdapter exporter = new PullingMetricsExporterAdapter("test");

        MetricsExportManager manager = MetricsFacade.createExportManager(exporter);
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
                .contains(
                        new MetricSnapshot(
                                singleSumDoubleMetric.metadata(),
                                List.of(new DataPointSnapshot(
                                        List.of(globalLabel),
                                        new DataPointSnapshot.ValueItem(1.3, new Label(DEFAULT_STAT_LABEL, "sum"))))),
                        new MetricSnapshot(
                                multipleDoubleStats.metadata(),
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
                .contains(
                        new MetricSnapshot(
                                singleSumDoubleMetric.metadata(),
                                List.of(new DataPointSnapshot(
                                        List.of(globalLabel),
                                        new DataPointSnapshot.ValueItem(3.0, new Label(DEFAULT_STAT_LABEL, "sum"))))),
                        new MetricSnapshot(
                                multipleDoubleStats.metadata(),
                                List.of(new DataPointSnapshot(
                                        List.of(globalLabel),
                                        new DataPointSnapshot.ValueItem(3.0, new Label(DEFAULT_STAT_LABEL, "sum")),
                                        new DataPointSnapshot.ValueItem(
                                                5.0, new Label(DEFAULT_STAT_LABEL, "sumPlusOne"))))));

        manager.shutdown();
    }
}*/
