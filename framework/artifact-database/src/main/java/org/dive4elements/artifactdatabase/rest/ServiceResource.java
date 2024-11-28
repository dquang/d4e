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

import org.dive4elements.artifacts.Service;

/**
 * Resource to process incoming XML documents with a given service.
 *
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class ServiceResource
extends      BaseResource
{
    private static Logger logger = LogManager.getLogger(ServiceResource.class);

    /**
     * server URL where to reach the resource.
     */
    public static final String PATH = "/service/{service}";

    /**
     * Error message if no corresponing service is provided by
     * the artifact database.
     */
    public static final String NO_SUCH_ACTION_MESSAGE = "no such service";

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

        String service = (String)request.getAttributes().get("service");

        ArtifactDatabase db = (ArtifactDatabase)getContext()
            .getAttributes().get("database");

        try {
            return guessRepresentation(
                db.process(service, inputDocument, getCallMeta()));
        }
        catch (ArtifactDatabaseException adbe) {
            logger.warn(adbe.getLocalizedMessage(), adbe);
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_BAD_REQUEST, adbe.getMessage());
            return new EmptyRepresentation();
        }
    }

    protected static Representation guessRepresentation(Service.Output output) {

        MediaType mediaType = new MediaType(output.getMIMEType());
        Object    data      = output.getData();

        if (data instanceof Document) {
            return new DomRepresentation(mediaType, (Document)data);
        }

        if (data instanceof byte []) {
            return new ByteArrayRepresentation(mediaType, (byte [])data);
        }

        return new EmptyRepresentation();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
