package org.hiero.metrics.api.core;

import java.util.List;

public record DataPointSnapshot(MetricMetadata metadata, Object value, PrimitiveDataType dataType, List<Label> labels) {
}
