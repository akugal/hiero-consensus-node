// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.utils;

import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import org.hiero.metrics.api.core.Label;

/**
 * Utility class for metrics-related operations.
 */
public final class MetricUtils {

    private MetricUtils() {}

    /**
     * Converts an array of Label objects into a List, ensuring no duplicate label names.
     *
     * @param labels the array of Label objects
     * @return a List of Label objects
     * @throws IllegalArgumentException if there are duplicate label names
     */
    public static List<Label> asList(Label... labels) {
        if (labels == null || labels.length == 0) {
            return List.of();
        }

        HashSet<String> labelNames = new HashSet<>(labels.length);
        for (Label label : labels) {
            if (!labelNames.add(label.name())) {
                throw new IllegalArgumentException("Duplicate label name: " + label.name());
            }
        }

        return List.of(labels);
    }

    /**
     * Loads implementations of the specified class using Java's ServiceLoader mechanism.
     *
     * @param serviceType   the class of the implementations to load
     * @param <T>           the type of the implementation
     * @return a list of loaded implementations
     */
    public static <T> List<T> load(Class<T> serviceType) {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(serviceType);
        return serviceLoader.stream().map(ServiceLoader.Provider::get).toList();
    }
}
