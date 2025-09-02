// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import com.swirlds.base.ArgumentUtils;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A label is an immutable key-value pair that can be associated with a metric to provide additional context or
 * metadata. Labels are often used to differentiate between different instances of the same metric,
 * such as tracking the number of requests to a web server by different HTTP methods (e.g., GET,
 * POST, etc.) or by different response status codes (e.g., 200, 404, 500, etc.).
 */
public record Label(@NonNull String name, @NonNull String value) implements Comparable<Label> {

    /**
     * Constructs a new label with the specified name and value.
     *
     * @param name  the name of the label, must not be blank
     * @param value the value of the label, must not be blank
     */
    public Label(@NonNull String name, @NonNull String value) {
        this.name = ArgumentUtils.throwArgBlank(name, "labelName");
        this.value = ArgumentUtils.throwArgBlank(value, "labelValue");
    }

    @Override
    public int compareTo(Label other) {
        int nameCompare = name.compareTo(other.name);
        return nameCompare != 0 ? nameCompare : value.compareTo(other.value);
    }
}
