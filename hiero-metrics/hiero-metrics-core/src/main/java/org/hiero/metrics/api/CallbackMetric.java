// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.Metric;
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

    static Builder builder(MetricKey<CallbackMetric> key) {
        return new Builder(key);
    }

    CallbackMetric registerDataPoint(DoubleSupplier valueSupplier, Map<String, String> labels);

    @Override
    default void reset() {
        // no op
    }

    final class Builder extends Metric.Builder<Builder, CallbackMetric> {

        private final Map<Map<String, String>, DoubleSupplier> labeledDataPoints = new HashMap<>();

        private Builder(MetricKey<CallbackMetric> key) {
            super(key);
        }

        public Builder registerDataPoint(@NonNull DoubleSupplier valueSupplier, Map<String, String> labels) {
            // labels should be validated in metric during registration
            if (labeledDataPoints.put(labels, valueSupplier) != null) {
                throw new IllegalArgumentException("A data point with the same label values already exists: " + labels);
            }
            return this;
        }

        public Map<Map<String, String>, DoubleSupplier> getLabeledDataPoints() {
            return labeledDataPoints;
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
