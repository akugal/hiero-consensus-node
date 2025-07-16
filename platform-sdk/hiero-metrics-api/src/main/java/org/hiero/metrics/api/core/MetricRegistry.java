// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.hiero.metrics.api.core.snapshot.MetricSnapshot;

public class MetricRegistry {

    private static class Holder {
        private static final MetricRegistry INSTANCE = new MetricRegistry();
    }

    private final List<Label> globalLabels;
    private final ConcurrentHashMap<MetricMetadata, Metric> metrics = new ConcurrentHashMap<>();

    public MetricRegistry(Label... globalLabels) {
        this.globalLabels = List.of(globalLabels);

        // verify no duplicate names
        Set<String> set = this.globalLabels.stream().map(Label::getName).collect(Collectors.toSet());
        if (set.size() != this.globalLabels.size()) {
            throw new IllegalArgumentException("Global labels must not contain duplicates");
        }
    }

    @NonNull
    public static MetricRegistry getDefault() {
        return Holder.INSTANCE;
    }

    public void reset() {
        metrics.values().parallelStream().forEach(Metric::reset);
    }

    @NonNull
    public List<Label> getGlobalLabels() {
        return globalLabels;
    }

    public List<MetricSnapshot> snapshot() {
        return metrics.values().stream()
                .map(metric -> new MetricSnapshot(metric.getMetadata(), metric.snapshotDataPoints()))
                .sorted(MetricSnapshot.COMPARATOR)
                .collect(Collectors.toList());
    }

    @NonNull
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
