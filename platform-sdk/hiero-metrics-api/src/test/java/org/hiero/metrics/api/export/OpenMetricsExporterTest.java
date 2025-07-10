package org.hiero.metrics.api.export;

import org.hiero.metrics.api.BooleanGauge;
import org.hiero.metrics.api.CallbackMetric;
import org.hiero.metrics.api.LongCounter;
import org.hiero.metrics.api.StatContainer;
import org.hiero.metrics.api.StatsGaugeAdapter;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.MetricRegistry;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OpenMetricsExporterTest {

    private final OpenMetricsExporter exporter = new OpenMetricsExporter();

    @Test
    public void export() throws IOException {
        BooleanGauge booleanGauge = BooleanGauge.builder("test_boolean_gauge")
                .withDescription("A test boolean gauge")
                .register();
        booleanGauge.update(true);

        LongCounter longCounter = LongCounter.builder("test_long_counter")
                .withUnit("requests")
                .register();
        longCounter.increment(42);

        CallbackMetric.builder("test_callback_metric")
                .withDynamicLabelNames("label1", "label2")
                .withCallback(callback -> {
                    callback.call(123.45, "val1", "val2");
                    callback.call(1.0, "1", "2");
                })
                .register();

        StatsGaugeAdapter<StatContainer> statGauge = StatsGaugeAdapter.<StatContainer>builder("test_stats_gauge")
                .withConstantLabel(new Label("env", "test"))
                .withUnit("ms")
                .withContainerFactory(StatContainer::new)
                .withStat("counter", StatContainer::getCounter)
                .withStat("sum", StatContainer::getSum)
                .withStat("average", StatContainer::getAverage)
                .withReset(StatContainer::reset)
                .register();
        statGauge.get().update(3);
        statGauge.get().update(5);


        // Use System.out directly without BufferedOutputStream wrapper
        exporter.export(MetricRegistry.getDefault().snapshot(), System.out);
        System.out.flush();
    }
}
