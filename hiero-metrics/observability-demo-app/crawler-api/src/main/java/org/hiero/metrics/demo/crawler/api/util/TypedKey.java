// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.util;

import java.util.Objects;

public class TypedKey<T> {

    private final String name;
    private final Class<T> type;

    public TypedKey(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    public static TypedKey<String> of(String name) {
        return new TypedKey<>(name, String.class);
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    public T cast(Object value) {
        return type.cast(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TypedKey<?>) obj;
        return Objects.equals(this.name, that.name) && Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return name + '[' + type.getSimpleName() + ']';
    }
}
