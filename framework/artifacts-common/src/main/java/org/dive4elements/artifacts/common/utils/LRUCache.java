package org.dive4elements.artifacts.common.utils;

import java.util.Map;
import java.util.LinkedHashMap;

public class LRUCache<K, V>
extends      LinkedHashMap<K, V>
{
    public static final int DEFAULT_MAX_CAPACITY = 25;

    private int maxCapacity;

    public LRUCache() {
        this(DEFAULT_MAX_CAPACITY);
    }

    public LRUCache(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
