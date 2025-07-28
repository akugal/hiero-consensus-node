// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import java.io.IOException;
import org.hiero.metrics.api.BooleanGauge;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.StatContainer;
import org.hiero.metrics.api.StatsGaugeAdapter;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.hiero.metrics.api.core.MetricsFacade;
import org.hiero.metrics.api.snapshot.MetricsSnapshotManager;
import org.hiero.metrics.api.snapshot.extension.OpenMetricsSnapshotsWriter;
import org.hiero.metrics.api.snapshot.extension.PushingMetricsExporterWriterAdapter;
import org.junit.jupiter.api.Test;

public class OpenMetricsSnapshotsWriterTest {

    @Test
    public void export() throws IOException, InterruptedException {
        MetricRegistry registry = MetricsFacade.createRegistry();
        MetricsSnapshotManager snapshotManager = MetricsFacade.createSnapshotManager(
                new PushingMetricsExporterWriterAdapter("console", new OpenMetricsSnapshotsWriter(), () -> System.out),
                1);
        snapshotManager.manageMetricRegistry(registry);

        BooleanGauge booleanGauge = BooleanGauge.builder(BooleanGauge.key("test_boolean_gauge"))
                .withDescription("A test boolean gauge")
                .register(registry);
        booleanGauge.getNotLabeled().update(true);

        LongCounter longCounter = LongCounter.builder(LongCounter.key("test_long_counter"))
                .withUnit("requests")
                .register(registry);
        longCounter.getNotLabeled().increment(42);

        CallbackMetric.builder(CallbackMetric.key("test_callback_metric"), callback -> {
                    callback.call(123.45, "val1", "val2");
                    callback.call(1.0, "1", "2");
                })
                .withDynamicLabelNames("label1", "label2")
                .register(registry);

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
