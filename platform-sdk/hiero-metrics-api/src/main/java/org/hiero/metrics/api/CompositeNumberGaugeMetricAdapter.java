package org.hiero.metrics.api;

import org.hiero.metrics.api.core.DataPointSnapshot;
import org.hiero.metrics.api.core.Label;
import org.hiero.metrics.api.core.PrimitiveDataType;
import org.hiero.metrics.api.core.StatefulMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class CompositeNumberGaugeMetricAdapter<D>
        extends StatefulMetric<D>
        implements Supplier<D> {

    private final String propertyLabel;
    private final String[] propertyNames;
    private final Function<D, Number[]> valuesGetter;

    private CompositeNumberGaugeMetricAdapter(Builder<D> builder) {
        super(builder);

        propertyLabel = requireNonNull(builder.propertyLabel, "Property label must not be null");
        propertyNames = requireNonNull(builder.propertyNames, "Property names must not be null");
        valuesGetter = requireNonNull(builder.valuesGetter, "Value getters must not be null");
    }

    public static <D> Builder<D> builder(String name) {
        return new Builder<>(name);
    }

    @Override
    public D get() {
        return getNoLabels();
    }

    @Override
    protected List<DataPointSnapshot> createSnapshots(D datapoint, List<String> dynamicLabelValues) {
        Number[] values = valuesGetter.apply(datapoint);
        if (values == null || values.length == 0) {
            throw new IllegalStateException("Values cannot be null or empty for " + getMetadata().name());
        }
        if (values.length != propertyNames.length) {
            throw new IllegalStateException("Values length does not match value names length for " + getMetadata().name());
        }

        List<DataPointSnapshot> snapshots = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            snapshots.add(createSnapshot(values[i], PrimitiveDataType.DOUBLE, dynamicLabelValues, new Label(propertyLabel, propertyNames[i])));
        }

        return snapshots;
    }

    public static class Builder<D>
            extends StatefulMetric.Builder<D, Builder<D>, CompositeNumberGaugeMetricAdapter<D>> {

        private String propertyLabel;
        private String[] propertyNames;
        private Function<D, Number[]> valuesGetter;

        private Builder(String name) {
            super(name);
        }

        public Builder<D> withPropertyLabel(String propertyLabel) {
            this.propertyLabel = propertyLabel;
            return this;
        }

        public Builder<D> withPropertyNames(String... propertyNames) {
            Set.of(propertyNames); // ensure no duplicates
            this.propertyNames = propertyNames;
            return this;
        }

        public Builder<D> withValuesGetter(Function<D, Number[]> valuesGetter) {
            this.valuesGetter = valuesGetter;
            return this;
        }

        @Override
        public Builder<D> withContainerFactory(Supplier<D> valueContainerFactory) {
            return super.withContainerFactory(valueContainerFactory);
        }

        @Override
        protected CompositeNumberGaugeMetricAdapter<D> buildMetric() {
            return new CompositeNumberGaugeMetricAdapter<>(this);
        }

        @Override
        protected Builder<D> self() {
            return this;
        }
    }
}
