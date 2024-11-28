/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.ArtifactNamespaceContext;

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

/**
 * Resource to create a new artifact within artifact database.
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class CreateResource
extends      BaseResource
{
    private static Logger logger = LogManager.getLogger(CreateResource.class);

    /**
     * server URL where to reach the resource.
     */
    public static final String PATH = "/create";

    /**
     * XPATH to figure out the name of the factory which should be used
     * to create the new artifact.
     */
    public static final String XPATH_FACTORY = "/art:action/art:factory/@name";

    /**
     * Error message if no factory was given.
     */
    public static final String NO_FACTORY_MESSAGE = "No factory given";

    /**
     * Error message if no artifact was created.
     */
    public static final String NO_ARTIFACT_CREATED = "No artifact created";

    @Override
    protected Representation innerPost(Representation requestRepr)
    throws ResourceException
    {
        Document inputDocument = null;
        try {
            DomRepresentation input = new DomRepresentation(requestRepr);
            input.setNamespaceAware(true);
            inputDocument = input.getDocument();
        }
        catch (IOException ioe) {
            logger.error(ioe.getMessage());
            Response response = getResponse();
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, ioe);
            return new EmptyRepresentation();
        }

        String factory = XMLUtils.xpathString(
            inputDocument,
            XPATH_FACTORY,
            ArtifactNamespaceContext.INSTANCE);

        if (factory == null || factory.length() == 0) {
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_BAD_REQUEST, NO_FACTORY_MESSAGE);
            return new EmptyRepresentation();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Create artifact with factory '" + factory + "'");
        }

        ArtifactDatabase db = getArtifactDatabase();

        try {
            return new DomRepresentation(
                MediaType.APPLICATION_XML,
                db.createArtifactWithFactory(factory,
                                             getCallMeta(),
                                             inputDocument));
        }
        catch (ArtifactDatabaseException adbe) {
            logger.error("Create artifact failed", adbe);
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, adbe.getMessage());
            return new EmptyRepresentation();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
