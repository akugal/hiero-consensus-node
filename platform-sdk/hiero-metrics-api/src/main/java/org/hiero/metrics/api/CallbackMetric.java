// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricCallback;
import org.hiero.metrics.api.core.PrimitiveDataType;

public final class CallbackMetric<T> extends Metric {

    private final PrimitiveDataType dataType;
    private final Consumer<MetricCallback<T>> callback;

    private CallbackMetric(Builder<T> builder) {
        super(builder);

        callback = Objects.requireNonNull(builder.callback, "Callback must not be null");
        dataType = Objects.requireNonNull(builder.dataType, "Data type must not be null");
    }

    public static <T> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    @Override
    public List<DataPointSnapshot> snapshot() {
        List<DataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept((value, labelValues) -> dataPoints.add(createSnapshot(value, dataType, List.of(labelValues))));
        return dataPoints;
    }

    public static class Builder<T> extends Metric.Builder<Builder<T>, CallbackMetric<T>> {

        private Consumer<MetricCallback<T>> callback;
        private PrimitiveDataType dataType;

        public Builder(String name) {
            super(name);
        }

        public Builder<T> withCallback(Class<T> valueType, Consumer<MetricCallback<T>> callback) {
            Objects.requireNonNull(valueType, "Type class cannot be null");
            this.dataType = PrimitiveDataType.mapDataType(valueType);
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
