/*
 * Copyright (c) 2011 by Intevation GmbH
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
import org.dive4elements.artifacts.CallMeta;

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

import org.restlet.resource.ResourceException;

import org.w3c.dom.Document;


/**
 * Base Resource to serve the out()-outputs of collections and artifacts.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation">Ingo Weinzierl</a>
 */
public abstract class BaseOutResource
extends               BaseResource
{
    /** The logger used in this class.*/
    private static Logger logger = LogManager.getLogger(BaseOutResource.class);

    /** XPath to figure out the MIME type of the requested result.*/
    public static final String XPATH_MIME_TYPE =
        "/art:action/art:out/art:mime-type/@value";

    /** Default result MIME type: octet stream.*/
    public static final MediaType DEFAULT_MIME_TYPE =
        MediaType.APPLICATION_OCTET_STREAM;


    @Override
    protected Representation innerPost(Representation requestRepr)
    throws    ResourceException
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

        ArtifactDatabase db = getArtifactDatabase();

        Request request = getRequest();

        String identifier = getIdentifier();
        String outType    = getType();

        if (logger.isDebugEnabled()) {
            logger.debug("looking for artifact id '" + identifier + "'");
        }

        String mimeTypeString = XMLUtils.xpathString(
            inputDocument,
            XPATH_MIME_TYPE,
            ArtifactNamespaceContext.INSTANCE);

        MediaType mimeType = DEFAULT_MIME_TYPE;

        if (mimeTypeString != null && mimeTypeString.length() != 0) {
            try {
                mimeType = MediaType.valueOf(mimeTypeString);
            }
            catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        try {
            return new OutRepresentation(
                mimeType,
                doOut(identifier, outType, inputDocument, db, getCallMeta()));
        }
        catch (ArtifactDatabaseException adbe) {
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_NOT_FOUND, adbe.getMessage());
            return new EmptyRepresentation();
        }
    }

    /**
     * Returns the identifier of the artifact or collection.
     *
     * @return the identifier.
     */
    protected abstract String getIdentifier();


    /**
     * Returns the concrete output type of the artifact or collection.
     *
     * @return the output type.
     */
    protected abstract String getType();

    /**
     * This method is called to process the operation on artifacts or
     * collections.
     *
     * @param identifier The identifier of the artifact or collection.
     * @param type The output type.
     * @param input The input document of the request.
     * @param db The artifact database.
     * @param meta The CallMeta object.
     *
     * @return the result of the operation.
     */
    protected abstract ArtifactDatabase.DeferredOutput doOut(
        String           identifier,
        String           type,
        Document         input,
        ArtifactDatabase db,
        CallMeta         meta)
    throws ArtifactDatabaseException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
