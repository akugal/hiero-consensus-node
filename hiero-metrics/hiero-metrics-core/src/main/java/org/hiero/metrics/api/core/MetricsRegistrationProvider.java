// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;

/**
 * An SPI for providing metrics to register in a {@link MetricRegistry}.
 */
public interface MetricsRegistrationProvider {

    /**
     * Returns a collection of metric builders to register.
     *
     * @return the metric builders, never {@code null}
     */
    @NonNull
    Collection<Metric.Builder<?, ?>> getMetricsToRegister();
}
