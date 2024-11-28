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

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.Request;
import org.restlet.Response;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

import org.restlet.ext.xml.DomRepresentation;

import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;

import org.w3c.dom.Document;

/**
 * Resource to import an XML document containg an artifact produced by
 * the ExportResource.
 *
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class ImportResource
extends      BaseResource
{
    private static Logger logger = LogManager.getLogger(ImportResource.class);

    /**
     * server URL where to reach the resource.
     */
    public static final String PATH = "/import";

    @Override
    protected Representation innerPost(Representation requestRepr) {

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

        Request request = getRequest();

        ArtifactDatabase db = (ArtifactDatabase)getContext()
            .getAttributes().get("database");

        try {
            return new DomRepresentation(
                MediaType.APPLICATION_XML,
                db.importArtifact(inputDocument, getCallMeta()));
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
