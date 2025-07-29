// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import org.hiero.metrics.api.core.Label;

public record DataPointSnapshot(@NonNull List<Label> labels, @NonNull List<ValueItem> valueItems) {

    public DataPointSnapshot(@NonNull List<Label> labels, @NonNull List<ValueItem> valueItems) {
        this.labels = List.copyOf(labels);
        this.valueItems = List.copyOf(valueItems);
        if (valueItems.isEmpty()) {
            throw new IllegalArgumentException("DataPointSnapshot must have at least one value item");
        }
    }

    public DataPointSnapshot(@NonNull List<Label> labels, ValueItem... valueItems) {
        this(labels, List.of(valueItems));
    }

    public DataPointSnapshot(ValueItem... valueItems) {
        this(List.of(), List.of(valueItems));
    }

    public DataPointSnapshot(@NonNull List<Label> labels, double value) {
        this(labels, List.of(new ValueItem(value)));
    }

    public record ValueItem(double value, @NonNull List<Label> labels) {

        public ValueItem(double value, @NonNull List<Label> labels) {
            this.value = value;
            this.labels = List.copyOf(labels);
        }

        public ValueItem(double value) {
            this(value, List.of());
        }

        public ValueItem(double value, Label... labels) {
            this(value, List.of(labels));
        }
    }
}
