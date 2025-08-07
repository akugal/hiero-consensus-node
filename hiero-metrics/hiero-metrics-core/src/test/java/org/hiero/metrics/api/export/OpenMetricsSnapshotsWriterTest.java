// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import java.io.IOException;
import java.util.Map;
import org.hiero.metrics.api.BooleanGauge;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.StatContainer;
import org.hiero.metrics.api.StatsGaugeAdapter;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;
import org.hiero.metrics.api.export.extension.OpenMetricsSnapshotsWriter;
import org.hiero.metrics.api.export.extension.PushingMetricsExporterWriterAdapter;
import org.junit.jupiter.api.Test;

public class OpenMetricsSnapshotsWriterTest {

    @Test
    public void export() throws IOException, InterruptedException {
        MetricRegistry registry = MetricsFacade.createRegistry();
        MetricsExportManager snapshotManager = MetricsFacade.createExportManager(
                new PushingMetricsExporterWriterAdapter("console", new OpenMetricsSnapshotsWriter(), () -> System.out),
                1);
        snapshotManager.manageMetricRegistry(registry);

        BooleanGauge booleanGauge = BooleanGauge.builder(BooleanGauge.key("test_boolean_gauge"))
                .withDescription("A test boolean gauge")
                .register(registry);
        booleanGauge.getNotLabeled().set(true);

        LongCounter longCounter = LongCounter.builder(LongCounter.key("test_long_counter"))
                .withUnit("requests")
                .register(registry);
        longCounter.getNotLabeled().increment(42);

        CallbackMetric.builder(CallbackMetric.key("test_callback_metric"))
                .withDynamicLabelNames("label1", "label2")
                .register(registry)
                .registerDataPoint(() -> 123.45, Map.of("label1", "val1", "label2", "val2"))
                .registerDataPoint(() -> 1.0, Map.of("label1", "1", "label2", "2"));

        StatsGaugeAdapter<StatContainer> statGauge = StatsGaugeAdapter.builder(
                        StatsGaugeAdapter.key("test_stats_gauge"), StatContainer::new)
                .withConstantLabel(new Label("env", "test"))
                .withUnit("ms")
                .withStat("counter", StatContainer::getCounter)
                .withStat("sum", StatContainer::getSum)
                .withStat("average", StatContainer::getAverage)
                .withReset(StatContainer::reset)
                .register(registry);
        statGauge.getNotLabeled().update(3);
        statGauge.getNotLabeled().update(5);

        Thread.sleep(1000);
    }
}
