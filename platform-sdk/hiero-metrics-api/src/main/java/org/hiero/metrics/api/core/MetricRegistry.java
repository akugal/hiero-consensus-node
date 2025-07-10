// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MetricRegistry {

    private static class Holder {
        private static final MetricRegistry INSTANCE = new MetricRegistry();
    }

    private final List<Label> globalLabels;
    private final ConcurrentHashMap<MetricMetadata, Metric> metrics = new ConcurrentHashMap<>();

    public MetricRegistry(Label... globalLabels) {
        Set.of(globalLabels); // verify no duplicates
        this.globalLabels = List.of(globalLabels);

        // verify no duplicate names
        Set<String> set = this.globalLabels.stream().map(Label::getName).collect(Collectors.toSet());
        if (set.size() != this.globalLabels.size()) {
            throw new IllegalArgumentException("Global labels must not contain duplicates");
        }
    }

    public void reset() {
        metrics.values().parallelStream().forEach(Metric::reset);
    }

    public static MetricRegistry getDefault() {
        return Holder.INSTANCE;
    }

    List<Label> getGlobalLabels() {
        return globalLabels;
    }

    public List<MetricSnapshot> snapshot() {
        return metrics.values().parallelStream()
                .map(metric -> new MetricSnapshot(metric.getMetadata(), metric.snapshot()))
                .sorted(Comparator.comparing(snapshot -> snapshot.metadata().getFullName()))
                .collect(Collectors.toList());
    }

    public <M extends Metric> M register(M metric) {
        Metric prev = metrics.putIfAbsent(metric.getMetadata(), metric);
        if (prev != null) {
            // TODO logging and ignore ?
            throw new IllegalArgumentException("Duplicate metric registration: "
                    + metric.getMetadata()
                    + ". Existing metric: "
                    + prev.getMetadata());
        }
        return metric;
    }
}
