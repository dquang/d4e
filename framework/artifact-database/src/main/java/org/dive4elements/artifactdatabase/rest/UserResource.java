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

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.Request;
import org.restlet.Response;

import org.w3c.dom.Document;

/**
 * A resource that handles actions to a specific user.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation">Ingo Weinzierl</a>
 */
public class UserResource
extends      BaseResource
{
    /** The logger that is used in this class. */
    private static Logger logger = LogManager.getLogger(UserResource.class);

    /** server URL where to reach the resource. */
    public static final String PATH = "/user/{uuid}";

    /**
     * XPath to figure out the type of action (feed, advance) via the
     * incoming POST request.
     */
    public static final String XPATH_ACTION = "/art:action/art:type/@name";

    /** Error message if no action was given. */
    public static final String NO_ACTION_MSG = "no action given";

    /** Error message if a unknown action was given. */
    public static final String NO_SUCH_ACTION_MSG = "no such action";

    /** Action name for deleting users. */
    public static final String ACTION_DELETE = "delete";


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
                Status.CLIENT_ERROR_BAD_REQUEST, NO_ACTION_MSG);
            return new EmptyRepresentation();
        }

        Request request = getRequest();

        String identifier = (String)request.getAttributes().get("uuid");

        ArtifactDatabase db = getArtifactDatabase();

        return dispatch(identifier, action, inputDocument, db);
    }

    /**
     * Method to figure out which POST action (feed or advance) was
     * triggered and perform this operation on the artifact specified
     * by 'identifier' and found in the artifact database 'db'
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
        ArtifactDatabase db)
    {
        Document out = null;

        logger.info("Action: " + action + " | User: " + identifier);

        try {
            if (action.equals(ACTION_DELETE)) {
                out = db.deleteUser(identifier, getCallMeta());
            }
            else {
                throw new ArtifactDatabaseException(NO_SUCH_ACTION_MSG);
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
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
