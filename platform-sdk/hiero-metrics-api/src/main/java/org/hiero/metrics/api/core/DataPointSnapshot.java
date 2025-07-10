// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.List;

public record DataPointSnapshot(long timestamp, String classifier, double value, List<Label> labels) {

    public DataPointSnapshot(String classifier, double value, List<Label> labels) {
        this(System.currentTimeMillis(), classifier, value, labels);
    }

    public DataPointSnapshot(double value, List<Label> labels) {
        this(null, value, labels);
    }

    public DataPointSnapshot(List<Label> labels) {
        this(null, MetricUtils.ZERO, labels);
    }
}
