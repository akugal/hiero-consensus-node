// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.api.core;

import com.swirlds.base.ArgumentUtils;
import java.util.Objects;

public final class Label implements Comparable<Label> {

    private final String name;
    private final String value;

    private final int hashCode;

    public Label(String name, String value) {
        this.name = ArgumentUtils.throwArgBlank(name, "labelName");
        this.value = ArgumentUtils.throwArgBlank(value, "labelValue");

        hashCode = Objects.hash(name, value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(Label other) {
        int nameCompare = name.compareTo(other.name);
        return nameCompare != 0 ? nameCompare : value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return "Label{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Label label = (Label) o;
        return Objects.equals(name, label.name) && Objects.equals(value, label.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
