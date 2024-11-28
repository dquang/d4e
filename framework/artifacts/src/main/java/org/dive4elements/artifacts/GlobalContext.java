package org.dive4elements.artifacts;

public interface GlobalContext {

    /**
     * Fetch a custom value from the global key/value map using
     * a given key.
     * @param key The key.
     * @return The stored value or null if no value was found under
     * this key.
     */
    Object get(Object key);

    /**
     * Store a custom key/value pair in the global map.
     * @param key The key to store
     * @param value The value to store
     * @return The old value registered under the key or null
     * if none wa there before.
     */
    Object put(Object key, Object value);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
