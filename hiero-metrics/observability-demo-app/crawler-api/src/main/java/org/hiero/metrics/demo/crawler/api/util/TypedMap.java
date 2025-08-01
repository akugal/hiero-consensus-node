// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class TypedMap {

    public static final TypedMap EMPTY = new TypedMap(Map.of());

    private final Map<String, Object> map;

    private TypedMap(Map<String, Object> map) {
        this.map = map;
    }

    public Map<String, Object> asMap() {
        return map;
    }

    public static TypedMap copy(TypedMap map) {
        return new TypedMap(Map.copyOf(map.map));
    }

    public static TypedMap copySorted(TypedMap map) {
        return new TypedMap(Map.copyOf(new TreeMap<>(map.map)));
    }

    public static TypedMap create() {
        return new TypedMap(new HashMap<>());
    }

    public static TypedMap createThreadSafe() {
        return new TypedMap(new ConcurrentHashMap<>());
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public <T> T computeIfAbsent(FactoryTypedKey<T> key) {
        return key.cast(map.computeIfAbsent(key.name(), k -> key.create()));
    }

    public <T> void put(TypedKey<T> key, T value) {
        map.put(key.name(), value);
    }

    public <T> T get(TypedKey<T> key) {
        return key.cast(map.get(key.name()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TypedMap typedMap = (TypedMap) o;
        return Objects.equals(map, typedMap.map);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
