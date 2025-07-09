// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.List;

public record DataPointSnapshot(String classifier, long createdTimeMillis, double value, List<Label> labels) {

    public DataPointSnapshot(String classifier, double value, List<Label> labels) {
        this(classifier, 0, value, labels);
    }

    public DataPointSnapshot(double value, List<Label> labels) {
        this(null, 0, value, labels);
    }

    public DataPointSnapshot(List<Label> labels) {
        this(null, 0, MetricUtils.ZERO, labels);
    }
}
