package org.hiero.metrics.api.export.api;

import org.hiero.metrics.api.core.snapshot.MetricSnapshot;

import java.util.List;
import java.util.function.Supplier;

public interface PullingMetricsExporter {

    String getName();

    void init(Supplier<List<MetricSnapshot>> snapshotSupplier);
}