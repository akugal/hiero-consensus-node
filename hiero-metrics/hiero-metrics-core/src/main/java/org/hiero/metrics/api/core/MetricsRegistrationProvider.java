// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;

/**
 * An SPI for providing metrics to register in a {@link MetricRegistry}.
 */
public interface MetricsRegistrationProvider {

    /**
     * @return a collection of metric builders to register, never {@code null}
     */
    @NonNull
    Collection<Metric.Builder<?, ?>> getMetricsToRegister();
}
