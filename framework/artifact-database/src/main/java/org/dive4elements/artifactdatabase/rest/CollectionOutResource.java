/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.CallMeta;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.Request;

import org.w3c.dom.Document;


/**
 * Resource to serve the out()-outputs of collections.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation">Ingo Weinzierl</a>
 */
public class CollectionOutResource
extends      BaseOutResource
{
    /** The logger used in this class.*/
    private static Logger logger = LogManager.getLogger(CollectionOutResource.class);

    /** server URL where to find the resource.*/
    public static final String PATH = "/collection/{uuid}/{type}";


    /**
     * Returns the identifier of the collection.
     *
     * @return the identifier of the collection.
     */
    protected String getIdentifier() {
        Request request = getRequest();

        return (String) request.getAttributes().get("uuid");
    }


    protected String getType() {
        Request request = getRequest();

        return (String) request.getAttributes().get("type");
    }


    /**
     * Call the ArtifactDatabase.outCollection method.
     */
    protected ArtifactDatabase.DeferredOutput doOut(
        String           identifier,
        String           type,
        Document         input,
        ArtifactDatabase db,
        CallMeta         meta)
    throws ArtifactDatabaseException
    {
        logger.debug("CollectionOutResource.doOut");

        return db.outCollection(identifier, type, input, meta);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
