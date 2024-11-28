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

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.Response;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;

/**
 * A Rest resource that finds the user provided by the artifact database.
 *
 */
public class FindUserResource
extends      BaseResource
{
    /** The logger that is used in this class.*/
    private static Logger logger = LogManager.getLogger(FindUserResource.class);

    /** server URL where to reach the resource.*/
    public static final String PATH = "/find-user";


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

        ArtifactDatabase db = getArtifactDatabase();

        try {
            logger.info(PATH);

            return new DomRepresentation(
                MediaType.APPLICATION_XML,
                db.findUser(input, getCallMeta()));
        }
        catch (ArtifactDatabaseException adbe) {
            logger.warn(adbe.getLocalizedMessage(), adbe);

            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, adbe.getMessage());
            return new EmptyRepresentation();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
