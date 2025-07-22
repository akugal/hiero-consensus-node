// SPDX-License-Identifier: Apache-2.0
module org.hiero.metrics.openmetrics.http {
    requires transitive org.hiero.metrics.core;
    requires jdk.httpserver;
    requires static transitive com.github.spotbugs.annotations;
    requires static transitive com.google.auto.service;

    provides org.hiero.metrics.api.snapshot.PullingMetricsExporter with
            org.hiero.metrics.openmetrics.OpenMetricsHttpEndpoint;
}
