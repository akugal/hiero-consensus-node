// SPDX-License-Identifier: Apache-2.0
package org.hiero.metrics.demo.crawler.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class FactoryTypedKey<T> extends TypedKey<T> {

    private final Supplier<T> factory;

    public FactoryTypedKey(String name, Class<T> type, Supplier<T> factory) {
        super(name, type);
        this.factory = factory;
    }

    @SuppressWarnings("unchecked")
    public static <T> FactoryTypedKey<List<T>> asList(String name) {
        return new FactoryTypedKey<>(name, (Class<List<T>>) (Class<?>) List.class, ArrayList::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> FactoryTypedKey<List<T>> asListThreadSafe(String name) {
        return new FactoryTypedKey<>(name, (Class<List<T>>) (Class<?>) List.class, CopyOnWriteArrayList::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> FactoryTypedKey<Set<T>> asSet(String name) {
        return new FactoryTypedKey<>(name, (Class<Set<T>>) (Class<?>) Set.class, HashSet::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> FactoryTypedKey<Set<T>> asSetThreadSafe(String name) {
        return new FactoryTypedKey<>(name, (Class<Set<T>>) (Class<?>) Set.class, ConcurrentHashMap::newKeySet);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> FactoryTypedKey<Map<K, V>> asForMap(String name) {
        return new FactoryTypedKey<>(name, (Class<Map<K, V>>) (Class<?>) Map.class, HashMap::new);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> FactoryTypedKey<Map<K, V>> asMapThreadSafe(String name) {
        return new FactoryTypedKey<>(name, (Class<Map<K, V>>) (Class<?>) Map.class, ConcurrentHashMap::new);
    }

    @SuppressWarnings("unchecked")
    public static FactoryTypedKey<TypedMap> asForTypedMap(String name) {
        return new FactoryTypedKey<>(name, TypedMap.class, TypedMap::create);
    }

    @SuppressWarnings("unchecked")
    public static FactoryTypedKey<TypedMap> asForTypedMapThreadSafe(String name) {
        return new FactoryTypedKey<>(name, TypedMap.class, TypedMap::createThreadSafe);
    }

    public T create() {
        return factory.get();
    }
}
