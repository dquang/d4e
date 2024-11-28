/*
 * Copyright (c) 2010, 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.DataProvider;


/**
 * Abstract class that implements some basic methods of a CallContext.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class AbstractCallContext implements CallContext {

    Logger logger = LogManager.getLogger(AbstractCallContext.class);

    /**
     * The ArtifactDatabase instance.
     */
    protected ArtifactDatabaseImpl database;

    /**
     * The action to be performed after the artifacts or collections calls.
     */
    protected int action;

    /**
     * The meta information of the concrete call (preferred languages et. al.)
     */
    protected CallMeta callMeta;

    /**
     * Map to act like a clipboard when nesting calls like a proxy artifact.
     */
    protected Map customValues;

    /**
     * Map to act like a clipboard when nesting calls like a proxy artifact.
     */
    protected Map<Object, List<DataProvider>> dataProviders;


    /**
     * The default constructor of this abstract CallContext.
     *
     * @param artifactDatabase The artifact database.
     * @param action The action.
     * @param callMeta The CallMeta object.
     */
    public AbstractCallContext(
        ArtifactDatabaseImpl artifactDatabase,
        int                  action,
        CallMeta             callMeta
    ) {
        this.database = artifactDatabase;
        this.action   = action;
        this.callMeta = callMeta;

        database.initCallContext(this);
    }


    public void postCall() {
        database.closeCallContext(this);
    }

    public abstract void afterCall(int action);

    public abstract Long getTimeToLive();

    public abstract void afterBackground(int action);


    public Object globalContext() {
        return database.context;
    }


    public ArtifactDatabase getDatabase() {
        return database;
    }


    public CallMeta getMeta() {
        return callMeta;
    }


    public Object getContextValue(Object key) {
        return customValues != null
            ? customValues.get(key)
            : null;
    }

    public Object putContextValue(Object key, Object value) {
        if (customValues == null) {
            customValues = new HashMap();
        }
        return customValues.put(key, value);
    }

    /**
     * Get list of DataProviders that registered for given key.
     * @return list (empty list if none found, never null).
     */
    public List<DataProvider> getDataProvider(Object key) {
        if (dataProviders != null) {
            List<DataProvider> list = dataProviders.get(key);
            return list != null
                ? list
                : java.util.Collections.<DataProvider>emptyList();
        }
        return java.util.Collections.<DataProvider>emptyList();
    }


    /**
     * Let a DataProvider register itself with given key.
     * Multiple DataProvider can register under the same key.
     */
    public Object registerDataProvider(Object key, DataProvider value) {
        List<DataProvider> providers = null;
        if (dataProviders == null) {
            dataProviders = new HashMap();
            providers = new ArrayList<DataProvider>();
            providers.add(value);
            return dataProviders.put(key, providers);
        }
        providers = dataProviders.get(key);

        if (providers == null) {
            providers = new ArrayList<DataProvider>();
        }
        providers.add(value);
        return dataProviders.put(key, providers);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
