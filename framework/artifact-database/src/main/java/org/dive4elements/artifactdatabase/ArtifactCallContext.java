/*
 * Copyright (c) 2010, 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import java.util.LinkedList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.Message;

import org.dive4elements.artifactdatabase.Backend.PersistentArtifact;


/**
 * Class that implements the call context handed to the methods calls
 * describe(), feed(), etc. of the artifact.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class ArtifactCallContext extends AbstractCallContext {

    private static Logger logger = LogManager.getLogger(ArtifactCallContext.class);


    /**
     * Error message issued if an artifact wants to translate itself
     * into a none valid persistent state.
     */
    public static final String INVALID_CALL_STATE = "Invalid after call state";

    /**
     * Error message issued if one tries to remove a requested artifact
     * from the list of artifacts running in background which is
     * not in this list.
     */
    public static final String NOT_IN_BACKGROUND = "Not in background";


    /**
     * The persistence wrapper around the living artifact
     */
    protected PersistentArtifact artifact;


    public ArtifactCallContext(
        ArtifactDatabaseImpl artifactDatabase,
        int                  action,
        CallMeta             callMeta,
        PersistentArtifact   artifact)
    {
        super(artifactDatabase, action, callMeta);

        this.artifact = artifact;
    }


    public void afterCall(int action) {
        this.action = action;
        if (action == BACKGROUND) {
            database.addIdToBackground(artifact.getId());
        }
    }


    public void afterBackground(int action) {
        if (this.action != BACKGROUND) {
            throw new IllegalStateException(NOT_IN_BACKGROUND);
        }
        database.fromBackground(artifact, action);
    }


    public boolean isInBackground() {
        return database.getLockedIds().contains(artifact.getId());
    }


    public void addBackgroundMessage(Message msg) {
        database.addBackgroundMessage(artifact.getArtifact().identifier(), msg);
    }


    public LinkedList<Message> getBackgroundMessages() {
        return database.getBackgroundMessages(
            artifact.getArtifact().identifier());
    }


    public Long getTimeToLive() {
        return artifact.getTTL();
    }


    /**
     * Dispatches and executes the persistence action after
     * the return of the concrete artifact call.
     */
    public void postCall() {
        try {
            switch (action) {
                case NOTHING:
                    break;
                case TOUCH:
                    artifact.touch();
                    break;
                case STORE:
                    artifact.store();
                    break;
                case BACKGROUND:
                    logger.warn(
                        "BACKGROUND processing is not fully implemented, yet!");
                    artifact.store();
                    break;
                default:
                    logger.error(INVALID_CALL_STATE + ": " + action);
                    throw new IllegalStateException(INVALID_CALL_STATE);
            }
        }
        finally {
            super.postCall();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
