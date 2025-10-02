// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.internal.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;
import java.util.function.Function;
import org.hiero.metrics.api.core.StatefulMetric;
import org.hiero.metrics.internal.datapoint.DataPointHolder;

public abstract class AbstractStatefulMetric<I, D> extends AbstractMetric<D> implements StatefulMetric<I, D> {

    private final I defaultInitializer;
    private final Function<I, D> dataPointFactory;

    @Nullable
    private final DataPointHolder<D> noLabelsDataPoint;

    protected AbstractStatefulMetric(StatefulMetric.Builder<I, D, ?, ?> builder) {
        super(builder);

        dataPointFactory = builder.getDataPointFactory();
        defaultInitializer = builder.getDefaultInitializer();

        if (dynamicLabelNames().isEmpty()) {
            noLabelsDataPoint = createDataPointHolder(dataPointFactory.apply(defaultInitializer), LabelValues.empty());
        } else {
            noLabelsDataPoint = null;
        }
    }

    protected abstract void reset(D dataPoint);

    @Override
    public final void reset() {
        if (noLabelsDataPoint != null) {
            reset(noLabelsDataPoint.dataPoint());
        } else {
            dataPoints.values().stream().map(DataPointHolder::dataPoint).forEach(this::reset);
        }
    }

    @NonNull
    @Override
    public final D getNotLabeled() {
        if (noLabelsDataPoint == null) {
            throw new IllegalStateException("This metric has dynamic labels, so you must call getOrCreateLabeled()");
        }
        return noLabelsDataPoint.dataPoint();
    }

    @Override
    public D getOrCreateLabeled(@NonNull String... namesAndValues) {
        if (noLabelsDataPoint != null) {
            throw new IllegalStateException("This metric has no dynamic labels, so you must call getNotLabeled()");
        }
        return dataPoints
                .computeIfAbsent(createLabelValues(namesAndValues), this::createDataPointHolder)
                .dataPoint();
    }

    @Override
    public D getOrCreateLabeled(@NonNull I initializer, @NonNull String... namesAndValues) {
        if (noLabelsDataPoint != null) {
            throw new IllegalStateException("This metric has no dynamic labels, so you must call getNotLabeled()");
        }
        Objects.requireNonNull(initializer);
        return dataPoints
                .computeIfAbsent(
                        createLabelValues(namesAndValues),
                        labelValues -> createDataPointHolder(labelValues, initializer))
                .dataPoint();
    }

    private DataPointHolder<D> createDataPointHolder(LabelValues labelValues) {
        return createDataPointHolder(labelValues, defaultInitializer);
    }

    private DataPointHolder<D> createDataPointHolder(LabelValues labelValues, @NonNull I initializer) {
        return createDataPointHolder(dataPointFactory.apply(initializer), labelValues);
    }
}
