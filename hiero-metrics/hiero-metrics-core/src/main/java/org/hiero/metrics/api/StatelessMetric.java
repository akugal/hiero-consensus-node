// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import org.hiero.metrics.api.core.Metric;
import org.hiero.metrics.api.core.MetricKey;
import org.hiero.metrics.api.core.MetricType;
import org.hiero.metrics.internal.DefaultStatelessMetric;

public interface StatelessMetric extends Metric {

    static MetricKey<StatelessMetric> key(String name) {
        return MetricKey.of(name, StatelessMetric.class);
    }

    static Builder builder(MetricKey<StatelessMetric> key) {
        return new Builder(key);
    }

    StatelessMetric registerDataPoint(DoubleSupplier valueSupplier, Map<String, String> labels);

    @Override
    default void reset() {
        // no op
    }

    final class Builder extends Metric.Builder<Builder, StatelessMetric> {

        private final Map<Map<String, String>, DoubleSupplier> labeledDataPoints = new HashMap<>();

        private Builder(MetricKey<StatelessMetric> key) {
            super(MetricType.GAUGE, key);
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

        @NonNull
        @Override
        protected StatelessMetric buildMetric() {
            return new DefaultStatelessMetric(this);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
