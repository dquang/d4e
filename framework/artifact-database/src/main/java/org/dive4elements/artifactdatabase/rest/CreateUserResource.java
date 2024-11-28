/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase.rest;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;


/**
 * Resource to create a new users within the artifact database.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation">Ingo Weinzierl</a>
 */
public class CreateUserResource
extends      BaseResource
{
    /** The logger used in this class.*/
    private static Logger logger = LogManager.getLogger(CreateUserResource.class);

    /** The URL part for this resource.*/
    public static final String PATH = "/create-user";


    @Override
    protected Representation innerPost(Representation requestRepr)
    throws    ResourceException
    {
        Document input = null;

        try {
            DomRepresentation in = new DomRepresentation(requestRepr);
            in.setNamespaceAware(true);
            input = in.getDocument();
        }
        catch (IOException ioe) {
            logger.error(ioe.getLocalizedMessage(), ioe);

            Response response = getResponse();
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
            return new EmptyRepresentation();
        }

        logger.debug("Create user");

        ArtifactDatabase db = getArtifactDatabase();

        try {
            return new DomRepresentation(
                MediaType.APPLICATION_XML,
                db.createUser(input, getCallMeta()));
        }
        catch (ArtifactDatabaseException adbe) {
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, adbe.getMessage());
            return new EmptyRepresentation();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
