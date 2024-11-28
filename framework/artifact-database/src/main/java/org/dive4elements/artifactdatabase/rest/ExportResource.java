/*
 * Copyright (c) 2010 by Intevation GmbH
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

import org.restlet.Request;
import org.restlet.Response;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

import org.restlet.ext.xml.DomRepresentation;

import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import org.restlet.resource.ResourceException;

/**
 * Resource to produce an external XML representation of a given
 * artifact to be import by ImportResource later on.
 *
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class ExportResource
extends      BaseResource
{
    private static Logger logger = LogManager.getLogger(ExportResource.class);

    /**
     * server URL where to reach the resource.
     */
    public static final String PATH = "/export/{uuid}";

    @Override
    protected Representation innerGet()
    throws                   ResourceException
    {
        Request request = getRequest();

        String identifier = (String)request.getAttributes().get("uuid");

        if (logger.isDebugEnabled()) {
            logger.debug("looking for artifact id '" + identifier + "'");
        }

        ArtifactDatabase db = (ArtifactDatabase)getContext()
            .getAttributes().get("database");

        try {
            return new DomRepresentation(
                MediaType.APPLICATION_XML,
                db.exportArtifact(identifier, getCallMeta()));
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
