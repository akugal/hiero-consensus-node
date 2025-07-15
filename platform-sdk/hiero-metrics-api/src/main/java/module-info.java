// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.api {
    exports org.hiero.metrics.api;
    exports org.hiero.metrics.api.core;
    exports org.hiero.metrics.api.core.snapshot;
    exports org.hiero.metrics.api.datapoint;
    exports org.hiero.metrics.api.export;

    requires com.swirlds.base;
    requires jdk.httpserver;
    requires static transitive com.github.spotbugs.annotations;
}
