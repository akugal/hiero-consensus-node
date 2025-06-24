package org.hiero.metrics.api.datapoint;

import java.util.function.BooleanSupplier;

public interface BooleanGaugeDataPoint extends BooleanSupplier {

    void update(boolean value);
}