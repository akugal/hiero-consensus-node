// SPDX-License-Identifier: Apache-2.0
import org.hiero.metrics.api.snapshot.PullingMetricsExporter;
import org.hiero.metrics.api.snapshot.PushingMetricsExporter;

// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.api {
    uses PullingMetricsExporter;
    uses PushingMetricsExporter;

    exports org.hiero.metrics.api;
    exports org.hiero.metrics.api.core;
    exports org.hiero.metrics.api.utils;
    exports org.hiero.metrics.api.datapoint;
    exports org.hiero.metrics.api.snapshot;
    exports org.hiero.metrics.api.snapshot.extension;
    exports org.hiero.metrics.api.snapshot.extension.impl;

    requires com.swirlds.base;
    requires jdk.httpserver;
    requires static transitive com.github.spotbugs.annotations;
}
