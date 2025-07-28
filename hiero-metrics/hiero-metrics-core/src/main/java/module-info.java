// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.core {
    uses org.hiero.metrics.api.snapshot.PullingMetricsExporter;
    uses org.hiero.metrics.api.snapshot.PushingMetricsExporter;

    exports org.hiero.metrics.api;
    exports org.hiero.metrics.api.core;
    exports org.hiero.metrics.api.utils;
    exports org.hiero.metrics.api.datapoint;
    exports org.hiero.metrics.api.snapshot;
    exports org.hiero.metrics.api.snapshot.extension;
    exports org.hiero.metrics.api.stat;
    exports org.hiero.metrics.api.stat.container;

    requires transitive com.swirlds.base;
    requires org.apache.logging.log4j;
    requires static transitive com.github.spotbugs.annotations;
}
