// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Interface for binding a {@link MetricRegistry} for metrics registration or retrieval.
 * <p>
 * Use {@link IdempotentMetricsBinder} abstract class for an idempotent and thread-safe implementation.
 */
public interface MetricsBinder {

    /**
     * Binds the provided {@link MetricRegistry}.
     * This method can be called during the initialization phase to register or retrieve metrics.
     *
     * @param registry the {@link MetricRegistry} to bind
     */
    void bind(@NonNull MetricRegistry registry);
}
