package org.hiero.metrics.api.export.api;

import org.hiero.metrics.api.core.snapshot.MetricSnapshot;

import java.io.IOException;
import java.util.List;

public interface PushingMetricsExporter {

    String getName();

    void export(List<MetricSnapshot> snapshots) throws IOException;
}