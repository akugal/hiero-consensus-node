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

public final class CallbackMetric extends Metric {

    private final Consumer<MetricCallback> callback;

    private CallbackMetric(Builder builder) {
        super(builder);

        callback = Objects.requireNonNull(builder.callback, "Callback must not be null");
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    @Override
    public void reset() {
        // no op
    }

    @NonNull
    @Override
    public List<DataPointSnapshot> snapshot() {
        List<DataPointSnapshot> dataPoints = new ArrayList<>();
        callback.accept((value, labelValues) -> dataPoints.add(createSnapshot(value, List.of(labelValues))));
        return dataPoints;
    }

    public static class Builder extends Metric.Builder<Builder, CallbackMetric> {

        private Consumer<MetricCallback> callback;

        public Builder(String name) {
            super(name);
        }

        @Override
        protected MetricType getType() {
            return MetricType.GAUGE;
        }

        public Builder withCallback(Consumer<MetricCallback> callback) {
            this.callback = Objects.requireNonNull(callback, "Callback consumer cannot be null");
            return this;
        }

        @Override
        protected CallbackMetric buildMetric() {
            return new CallbackMetric(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
