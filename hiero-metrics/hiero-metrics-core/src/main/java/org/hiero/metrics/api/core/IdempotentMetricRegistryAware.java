package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class IdempotentMetricRegistryAware implements MetricRegistryAware {

    private static final Logger logger = LogManager.getLogger(IdempotentMetricRegistryAware.class);

    private final AtomicBoolean metricsRegistered = new AtomicBoolean(false);

    @Override
    public final void registerMetrics(@NonNull MetricRegistry registry) {
        Objects.requireNonNull(registry, "metrics registry must not be null");

        if (metricsRegistered.compareAndSet(false, true)) {
            registerMetricsNonIdempotent(registry);
        } else  {
            logger.warn("Metrics already registered for instance of {}.", getClass().getName());
        }
    }

    protected abstract void registerMetricsNonIdempotent(@NonNull MetricRegistry registry);
}
