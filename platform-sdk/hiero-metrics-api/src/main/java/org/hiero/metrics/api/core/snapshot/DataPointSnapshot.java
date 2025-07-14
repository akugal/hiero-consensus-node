// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core.snapshot;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.Label;

public record DataPointSnapshot(@NonNull List<Label> labels, @NonNull List<ValueItem> valueItems) {

    public DataPointSnapshot(ValueItem... valueItems) {
        this(List.of(), List.of(valueItems));
    }

    public DataPointSnapshot(List<Label> labels, double value) {
        this(labels, List.of(new ValueItem(value)));
    }

    public record ValueItem(String classifier, double value, List<Label> labels) {

        public ValueItem(double value) {
            this(null, value, List.of());
        }

        public ValueItem(String classifier, double value) {
            this(classifier, value, List.of());
        }
    }
}
