/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import java.util.LinkedList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.Message;


/**
 * Class that implements the call context handed to ArtifactCollection specific
 * operations.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CollectionCallContext extends AbstractCallContext {

    private static Logger log = LogManager.getLogger(CollectionCallContext.class);

    /**
     * The ArtifactCollection.
     */
    protected ArtifactCollection collection;


    public CollectionCallContext(
        ArtifactDatabaseImpl artifactDatabase,
        int                  action,
        CallMeta             callMeta,
        ArtifactCollection   collection)
    {
        super(artifactDatabase, action, callMeta);

        this.collection = collection;
    }


    public void afterCall(int action) {
        log.debug("CollectionCallContext.afterCall - NOT IMPLEMENTED");
    }


    public void afterBackground(int action) {
        log.debug("CollectionCallContext.afterBackground - NOT IMPLEMENTED");
    }


    public boolean isInBackground() {
        log.debug("CollectionCallContext.isInBackground - NOT IMPLEMENTED");
        return false;
    }


    public void addBackgroundMessage(Message msg) {
        log.debug("CollectionCallContext.addBackgroundMessage NOT IMPLEMENTED");
    }


    public LinkedList<Message> getBackgroundMessages() {
        log.debug("CollectionCallContext.addBackgroundMessage NOT IMPLEMENTED");
        return null;
    }


    public Long getTimeToLive() {
        log.debug("CollectionCallContext.getTimeToLive - NOT IMPLEMENTED");
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
