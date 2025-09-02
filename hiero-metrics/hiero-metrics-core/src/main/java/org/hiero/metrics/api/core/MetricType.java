// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

/**
 * The type of metric that is compliant with OpenMetrics specification.
 */
public enum MetricType {
    UNKNOWN,
    COUNTER,
    GAUGE,
    STATE_SET,
    INFO
}
