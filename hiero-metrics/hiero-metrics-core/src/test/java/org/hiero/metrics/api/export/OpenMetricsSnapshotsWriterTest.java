// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import java.util.Map;
import java.util.function.IntSupplier;
import org.hiero.metrics.api.BooleanGauge;
import org.hiero.metrics.api.DoubleGauge;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.StatContainer;
import org.hiero.metrics.api.StatelessMetric;
import org.hiero.metrics.api.StatsGaugeAdapter;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;
import org.hiero.metrics.api.export.extension.PushingMetricsExporterWriterAdapter;
import org.hiero.metrics.api.export.extension.writer.OpenMetricsSnapshotsWriter;
import org.hiero.metrics.api.stat.StatUtils;
import org.junit.jupiter.api.Test;

public class OpenMetricsSnapshotsWriterTest {

    @Test
    public void write() throws InterruptedException {
        MetricRegistry registry = MetricsFacade.createRegistry(new Label("env", "test"));
        MetricsExportManager snapshotManager = MetricsFacade.createExportManager(
                new PushingMetricsExporterWriterAdapter("console", new OpenMetricsSnapshotsWriter(), () -> System.out),
                1);
        snapshotManager.manageMetricRegistry(registry);

        BooleanGauge booleanGauge = BooleanGauge.builder("boolean_gauge")
                .withDescription("A test boolean gauge without labels")
                .register(registry);
        booleanGauge.getNotLabeled().setTrue();

        StatelessMetric.builder(StatelessMetric.key("memory").withCategory("jvm"))
                .withDynamicLabelNames("type")
                .withDescription("JVM memory usage")
                .withUnit("bytes")
                .registerDataPoint(() -> Runtime.getRuntime().maxMemory(), Map.of("type", "max"))
                .registerDataPoint(() -> Runtime.getRuntime().totalMemory(), Map.of("type", "total"))
                .registerDataPoint(() -> Runtime.getRuntime().freeMemory(), Map.of("type", "free"))
                .register(registry);

        LongCounter longCounter = LongCounter.builder("test_long_counter")
                .withUnit("requests")
                .withConstantLabel(new Label("constant-label", "constant-value"))
                .withDynamicLabelNames("method")
                .register(registry);
        longCounter.getOrCreateLabeled(Map.of("method", "POST")).increment(42);
        longCounter.getOrCreateLabeled(Map.of("method", "GET")).increment(17);

        DoubleGauge doubleGauge = DoubleGauge.builder("test_double_gauge")
                .withOperator(StatUtils.DOUBLE_SUM, false)
                .withDynamicLabelNames("init")
                .register(registry);
        doubleGauge.getOrCreateLabeled(Map.of("init", "one"), () -> 1.0).update(10.0);
        doubleGauge.getOrCreateLabeled(Map.of("init", "default")).update(10.0);

        StatsGaugeAdapter<IntSupplier, StatContainer> statGauge = StatsGaugeAdapter.builder(
                        StatsGaugeAdapter.key("test_stats_gauge"), StatUtils.INT_INIT, StatContainer::new)
                .withConstantLabel(new Label("constant-label", "constant-value"))
                .withDynamicLabelNames("name")
                .withUnit("ms")
                .withStat("counter", StatContainer::getCounter)
                .withStat("sum", StatContainer::getSum)
                .withStat("average", StatContainer::getAverage)
                .withReset(StatContainer::reset)
                .register(registry);

        Map<String, String> labels1 = Map.of("name", "default");
        statGauge.getOrCreateLabeled(labels1).update(3);
        statGauge.getOrCreateLabeled(labels1).update(5);

        Map<String, String> labels2 = Map.of("name", "custom");
        statGauge.getOrCreateLabeled(labels2).update(10);
        statGauge.getOrCreateLabeled(labels2).update(2);

        Thread.sleep(1000);
    }
}
