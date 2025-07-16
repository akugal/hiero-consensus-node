// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.api {
    uses org.hiero.metrics.api.export.api.PullingMetricsExporter;
    uses org.hiero.metrics.api.export.api.PushingMetricsExporter;

    exports org.hiero.metrics.api;
    exports org.hiero.metrics.api.core;
    exports org.hiero.metrics.api.core.snapshot;
    exports org.hiero.metrics.api.datapoint;
    exports org.hiero.metrics.api.export;
    exports org.hiero.metrics.api.export.writer;
    exports org.hiero.metrics.api.export.api;

    requires com.swirlds.base;
    requires jdk.httpserver;
    requires static transitive com.github.spotbugs.annotations;
}
