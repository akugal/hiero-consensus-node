// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.function.Consumer;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricCallback;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.internal.DefaultCallbackMetric;

public interface CallbackMetric extends Metric {

    @Override
    default void reset() {
        // no op
    }

    static Builder builder(String name, Consumer<MetricCallback> callback) {
        return new Builder(name, callback);
    }

    final class Builder extends Metric.Builder<Builder, CallbackMetric> {

        private final Consumer<MetricCallback> callback;

        private Builder(String name, Consumer<MetricCallback> callback) {
            super(name);
            this.callback = Objects.requireNonNull(callback, "Callback consumer cannot be null");
        }

        @NonNull
        public Consumer<MetricCallback> getCallback() {
            return callback;
        }

        @Override
        public MetricType getType() {
            return MetricType.GAUGE;
        }

        @Override
        protected CallbackMetric buildMetric() {
            return new DefaultCallbackMetric(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
