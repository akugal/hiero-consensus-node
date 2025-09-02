// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IdempotentMetricRegistryAware implements MetricRegistryAware {

    private static final Logger logger = LogManager.getLogger(IdempotentMetricRegistryAware.class);

    private final AtomicBoolean metricsRegistered = new AtomicBoolean(false);

    @Override
    public final void registerMetrics(@NonNull MetricRegistry registry) {
        Objects.requireNonNull(registry, "metrics registry must not be null");

        if (metricsRegistered.compareAndSet(false, true)) {
            registerMetricsNonIdempotent(registry);
        } else {
            logger.warn(
                    "Metrics already registered for instance of {}.", getClass().getName());
        }
    }

    protected boolean isMetricsRegistered() {
        return metricsRegistered.get();
    }

    protected abstract void registerMetricsNonIdempotent(@NonNull MetricRegistry registry);
}
