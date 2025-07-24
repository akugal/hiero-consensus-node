// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.function.Consumer;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricCallback;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.internal.DefaultCallbackMetric;

public interface CallbackMetric extends Metric {

    static MetricKey<CallbackMetric> key(String name) {
        return MetricKey.of(name, CallbackMetric.class);
    }

    static MetricKey<CallbackMetric> key(String category, String name) {
        return MetricKey.of(category, name, CallbackMetric.class);
    }

    static Builder builder(MetricKey<CallbackMetric> key, Consumer<MetricCallback> callback) {
        return new Builder(key, callback);
    }

    @Override
    default void reset() {
        // no op
    }

    final class Builder extends Metric.Builder<Builder, CallbackMetric> {

        private final Consumer<MetricCallback> callback;

        private Builder(MetricKey<CallbackMetric> key, Consumer<MetricCallback> callback) {
            super(key);
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
