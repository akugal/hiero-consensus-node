// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.export.extension.writer;

import java.util.HashMap;
import java.util.Map;
import org.hiero.metrics.api.export.DataPointSnapshot;
import org.hiero.metrics.api.export.MetricSnapshot;

public abstract class BaseMetricExportData {

    private final MetricSnapshot metricSnapshot;
    private final Map<DataPointSnapshot, TemplateByteArray> dataPointCache = new HashMap<>();

    protected BaseMetricExportData(MetricSnapshot metricSnapshot) {
        this.metricSnapshot = metricSnapshot;
    }

    public final MetricSnapshot metricSnapshot() {
        return metricSnapshot;
    }

    public final void clearCache() {
        dataPointCache.clear();
    }

    public final TemplateByteArray getAndUpdateDataPointExportData(DataPointSnapshot dataPointSnapshot) {
        return dataPointCache.computeIfAbsent(dataPointSnapshot, this::buildDataPointExportData);
    }

    protected abstract TemplateByteArray buildDataPointExportData(DataPointSnapshot dataPointSnapshot);
}
