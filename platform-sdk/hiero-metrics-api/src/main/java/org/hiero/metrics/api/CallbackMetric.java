// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricCallback;
import org.hiero.metrics.api.core.MetricType;

public final class CallbackMetric<T extends Number> extends Metric {

    private final Consumer<MetricCallback<T>> callback;

    private CallbackMetric(Builder<T> builder) {
        super(builder);

        callback = Objects.requireNonNull(builder.callback, "Callback must not be null");
    }

    public static <T extends Number> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    @Override
    public void reset() {
        // no op
    }

    @NonNull
    @Override
    public List<DataPointSnapshot> snapshot() {
        List<DataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept(
                (value, labelValues) -> dataPoints.add(createSnapshot(value.doubleValue(), List.of(labelValues))));
        return dataPoints;
    }

    public static class Builder<T extends Number> extends Metric.Builder<Builder<T>, CallbackMetric<T>> {

        private Consumer<MetricCallback<T>> callback;

        public Builder(String name) {
            super(name);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
        }

        public Builder<T> withCallback(Consumer<MetricCallback<T>> callback) {
            this.callback = Objects.requireNonNull(callback, "Callback consumer cannot be null");
            return this;
        }

        @Override
        protected CallbackMetric<T> buildMetric() {
            return new CallbackMetric<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }
    }
}
