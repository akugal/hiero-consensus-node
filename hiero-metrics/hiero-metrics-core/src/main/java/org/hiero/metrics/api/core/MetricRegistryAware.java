package org.hiero.metrics.api.core;

public interface MetricRegistryAware {

    void registerMetrics(MetricRegistry registry);
}
