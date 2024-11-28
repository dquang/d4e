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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.Request;
import org.restlet.resource.ResourceException;
import org.restlet.Response;


/**
 * A Rest resource that lists the collections of a specific user provided by
 * the artifact database.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation">Ingo Weinzierl</a>
 */
public class ListCollectionsResource
extends      BaseResource
{
   /** The logger that is used in this class.*/
    private static Logger logger =
        LogManager.getLogger(ListCollectionsResource.class);

    /** server URL where to reach the resource.*/
    public static final String PATH = "/list-collections/{ownerid}";


    @Override
    protected Representation innerGet()
    throws                   ResourceException
    {
        Request request     = getRequest();

        String ownerId      = (String) request.getAttributes().get("ownerid");

        ArtifactDatabase db = getArtifactDatabase();

        try {
            logger.info("List collections owned by " + ownerId);

            return new DomRepresentation(
                MediaType.APPLICATION_XML,
                db.listCollections(ownerId, getCallMeta()));
        }
        catch (ArtifactDatabaseException adbe) {
            logger.warn(adbe.getLocalizedMessage(), adbe);

            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_NOT_FOUND, adbe.getMessage());
            return new EmptyRepresentation();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
