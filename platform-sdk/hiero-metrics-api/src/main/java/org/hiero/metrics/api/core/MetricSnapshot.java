// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.List;

public record MetricSnapshot(MetricMetadata metadata, List<DataPointSnapshot> dataPoints) {}
