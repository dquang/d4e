/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import java.util.HashMap;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.GlobalContext;

/**
 * Default implementation of the context.
 * Besides of the configuration it hosts a map to store key/value pairs.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultArtifactContext implements GlobalContext
{
    /**
     * The global configuration document of the artifact database.
     */
    protected Document config;

    /**
     * Custom key/value pairs to be used globally in the whole server.
     */
    protected HashMap map;

    /**
     * Default constructor.
     */
    public DefaultArtifactContext() {
        this(null);
    }

    /**
     * Constructor to create a context with a given global
     * configuration document and an empty map of custom
     * key/value pairs.
     * @param config
     */
    public DefaultArtifactContext(Document config) {
        this.config = config;
        map = new HashMap();
    }

    /**
     * Fetch a custom value from the global key/value map using
     * a given key.
     * @param key The key.
     * @return The stored value or null if no value was found under
     * this key.
     */
    public synchronized Object get(Object key) {
        return map.get(key);
    }

    /**
     * Store a custom key/value pair in the global map.
     * @param key The key to store
     * @param value The value to store
     * @return The old value registered under the key or null
     * if none wa there before.
     */
    public synchronized Object put(Object key, Object value) {
        return map.put(key, value);
    }

    /**
     * Returns a reference to the global configuration document.
     * @return The global configuration document.
     */
    public Document getConfig() {
        return config;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
