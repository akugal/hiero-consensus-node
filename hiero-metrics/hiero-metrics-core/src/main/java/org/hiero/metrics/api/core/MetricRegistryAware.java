// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

public interface MetricRegistryAware {

    void registerMetrics(MetricRegistry registry);
}
