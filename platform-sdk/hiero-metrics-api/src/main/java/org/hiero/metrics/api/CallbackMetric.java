// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.hiero.metrics.api.core.Callback;
import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.PrimitiveDataType;

public final class CallbackMetric<T> extends Metric {

    private final PrimitiveDataType dataType;
    private final Consumer<Callback<T>> callback;

    private CallbackMetric(Builder<T> builder) {
        super(builder);

        requireNonNull(builder.type, "Type class cannot be null");

        callback = requireNonNull(builder.callback, "Callback cannot be null");
        dataType = PrimitiveDataType.mapDataType(builder.type);
    }

    public static <T> Builder<T> builder(String name) {
        return new Builder<>(name);
    }

    @Override
    public List<DataPointSnapshot> snapshot() {
        List<DataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept(
                (value, labelValues) -> dataPoints.add(createSnapshot(value, dataType, Arrays.asList(labelValues))));
        return dataPoints;
    }

    public static class Builder<T> extends Metric.Builder<Builder<T>, CallbackMetric<T>> {

        private Consumer<Callback<T>> callback;
        private Class<T> type;

        public Builder(String name) {
            super(name);
        }

        @Override
        protected CallbackMetric<T> buildMetric() {
            return new CallbackMetric<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        public Builder<T> withCallback(Class<T> type, Consumer<Callback<T>> callback) {
            this.type = type;
            this.callback = callback;
            return this;
        }
    }
}
