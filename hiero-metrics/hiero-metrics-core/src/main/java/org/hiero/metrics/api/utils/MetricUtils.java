// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.utils;

import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import org.hiero.metrics.api.core.Label;

public final class MetricUtils {

    private MetricUtils() {}

    public static List<Label> asList(Label... labels) {
        if (labels == null || labels.length == 0) {
            return List.of();
        }

        HashSet<String> labelNames = new HashSet<>(labels.length);
        for (Label label : labels) {
            if (!labelNames.add(label.getName())) {
                throw new IllegalArgumentException("Duplicate label name: " + label.getName());
            }
        }

        return List.of(labels);
    }

    public static <E> List<E> load(Class<E> exporterClass) {
        ServiceLoader<E> serviceLoader = ServiceLoader.load(exporterClass);
        return serviceLoader.stream().map(ServiceLoader.Provider::get).toList();
    }
}
