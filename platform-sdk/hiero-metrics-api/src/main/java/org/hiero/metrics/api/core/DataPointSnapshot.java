// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.List;

public record DataPointSnapshot(
        MetricMetadata metadata, long createdTimeMillis, Object value, PrimitiveDataType dataType, List<Label> labels) {

    public DataPointSnapshot(MetricMetadata metadata, Object value, PrimitiveDataType dataType, List<Label> labels) {
        this(metadata, 0, value, dataType, labels);
    }
}