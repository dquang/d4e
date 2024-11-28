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
 * Resource to expose the core artifact methods
 * (describe, feed and advance) via REST.
 *
 * <ul>
 * <li>describe() is modelled via GET.</li>
 * <li>advance() and feed() are modelled via POST.</li>
 * </ul>
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class ArtifactResource
extends      BaseResource
{
    private static Logger logger = LogManager.getLogger(ArtifactResource.class);

    /**
     * XPath to figure out the type of action (feed, advance) via the
     * incoming POST request.
     */
    public static final String XPATH_ACTION = "/art:action/art:type/@name";

    /**
     * server URL where to reach the resource.
     */
    public static final String PATH = "/artifact/{uuid}";

    /**
     * Error message if no action was given.
     */
    public static final String NO_ACTION_MESSAGE      = "no action given";

    /**
     * Error message if a unknown action was given.
     */
    public static final String NO_SUCH_ACTION_MESSAGE = "no such action";

    /**
     * Error message if the requested artifact was not found in
     * the artifact database.
     */
    public static final String NO_ARTIFACT_FOUND = "Artifact not found";

    /**
     * Action name 'advance'.
     */
    public static final String ADVANCE  = "advance";
    /**
     * Action name 'feed'.
     */
    public static final String FEED     = "feed";
    /**
     * Action name 'describe'.
     */
    public static final String DESCRIBE = "describe";

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
                db.describe(identifier, null, getCallMeta()));
        }
        catch (ArtifactDatabaseException adbe) {
            logger.warn(adbe.getLocalizedMessage(), adbe);
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_NOT_FOUND, adbe.getMessage());
            return new EmptyRepresentation();
        }
    }

    /**
     * Method to figure out which POST action (feed or advance) was
     * triggered and perform this operation on the artifact specified
     * by 'identifier' and found in the artifact database 'db'.
     *
     * @param identifier The identifier of the artifact.
     * @param action The action to be performed.
     * @param source The input document to further parameterize the
     * operation.
     * @param db The artifact database where to find the artifact.
     * @return The representation produced by the performed action.
     */
    protected Representation dispatch(
        String           identifier,
        String           action,
        Document         source,
        ArtifactDatabase db
    ) {
        Document out = null;

        try {
            if (action.equals(FEED)) {
                out = db.feed(identifier, source, getCallMeta());
            }
            else if (action.equals(ADVANCE)) {
                out = db.advance(identifier, source, getCallMeta());
            }
            else if (action.equals(DESCRIBE)) {
                out = db.describe(identifier, source, getCallMeta());
            }
            else {
                throw new ArtifactDatabaseException(NO_SUCH_ACTION_MESSAGE);
            }
        }
        catch (ArtifactDatabaseException adbe) {
            logger.warn(adbe.getLocalizedMessage(), adbe);
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_BAD_REQUEST, adbe.getMessage());
            return new EmptyRepresentation();
        }

        return new DomRepresentation(MediaType.APPLICATION_XML, out);
    }

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

        String action = XMLUtils.xpathString(
            inputDocument,
            XPATH_ACTION,
            ArtifactNamespaceContext.INSTANCE);

        if (action == null || action.length() == 0) {
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_BAD_REQUEST, NO_ACTION_MESSAGE);
            return new EmptyRepresentation();
        }

        Request request = getRequest();

        String identifier = (String)request.getAttributes().get("uuid");

        ArtifactDatabase db = (ArtifactDatabase)getContext()
            .getAttributes().get("database");

        return dispatch(identifier, action, inputDocument, db);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
