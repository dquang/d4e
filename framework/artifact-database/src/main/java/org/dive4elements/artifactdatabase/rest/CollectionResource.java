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
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.ArtifactDatabaseImpl;

import java.io.IOException;

import javax.xml.xpath.XPathConstants;

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
import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation">Ingo Weinzierl</a>
 */
public class CollectionResource
extends      BaseResource
{
    /** The logger that is used in this class.*/
    private static Logger logger = LogManager.getLogger(CollectionResource.class);

    /** server URL where to reach the resource.*/
    public static final String PATH = "/collection/{uuid}";

    /**
     * XPath to figure out the type of action (feed, advance) via the
     * incoming POST request.
     */
    public static final String XPATH_ACTION = "/art:action/art:type/@name";

    /**
     * XPath to figure out the identifier of the artifact described in the
     * action.
     */
    public static final String XPATH_ARTIFACT =
        "/art:action/art:type/art:artifact/@uuid";

    /** Error message if no action was given.*/
    public static final String NO_ACTION_MSG = "no action given";

    /** Error message if a unknown action was given.*/
    public static final String NO_SUCH_ACTION_MSG = "no such action";

    /** Action name for deleting a collection.*/
    public static final String ACTION_DELETE = "delete";

    /** Action name for describing the collection.*/
    public static final String ACTION_DESCRIBE = "describe";

    /** Action name for retrieving the attribute of a collection.*/
    public static final String ACTION_GET_ATTRIBUTE = "getattribute";

    /** Action name for retrieving the attributes of an artifact stored in the
     * collection.*/
    public static final String ACTION_GET_ITEM_ATTRIBUTE = "getitemattribute";

    /** Action name for setting the attribute of a collection.*/
    public static final String ACTION_SET_ATTRIBUTE = "setattribute";

    /** Action name for setting the attribute for an artifact stored in the
     * collection.*/
    public static final String ACTION_SET_ITEM_ATTRIBUTE = "setitemattribute";

    /** Action name for adding a new artifact to the collection.*/
    public static final String ACTION_ADD_ARTIFACT = "addartifact";

    /** Action name for removing an artifact from the collection.*/
    public static final String ACTION_REMOVE_ARTIFACT = "removeartifact";

    /** Action name for listing the artifacts of the collection.*/
    public static final String ACTION_LIST_ARTIFACTS = "listartifacts";

    /** Action name for setting the ttl of a collection.*/
    public static final String ACTION_SET_TTL = "settimetolive";

    /** Action name for setting the name of a collection.*/
    public static final String ACTION_SET_NAME = "setname";


    /**
     * Method to figure out which POST action was triggered and perform this
     * operation on the collection specified by 'identifier' and found in the
     * artifact database 'db'.
     *
     * @param identifier The identifier of the collection.
     * @param action The action to be performed.
     * @param source The input document to further parameterize the operation.
     * @param db The artifact database where to find the collection.
     *
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
            CallMeta meta = getCallMeta();

            if (action.equals(ACTION_DELETE)) {
                logger.info("Delete collection '" + identifier + "'");
                out = db.deleteCollection(identifier, getCallMeta());
            }
            else if (action.equals(ACTION_DESCRIBE)) {
                logger.info("Describe collection '" + identifier + "'");

                out = db.describeCollection(identifier, meta);
            }
            else if (action.equals(ACTION_ADD_ARTIFACT)) {
                String art = getArtifactIdentifier(source);

                logger.info("Add artifact '" + art + "' to collection.");
                out = db.addCollectionArtifact(identifier, art, source, meta);
            }
            else if (action.equals(ACTION_REMOVE_ARTIFACT)) {
                String art = getArtifactIdentifier(source);

                logger.info("Remove artifact '" + art + "' from collection.");
                out = db.removeCollectionArtifact(identifier, art, meta);
            }
            else if (action.equals(ACTION_LIST_ARTIFACTS)) {
                logger.info("List artifacts of collection '" + identifier +"'");
                out = db.listCollectionArtifacts(identifier, meta);
            }
            else if (action.equals(ACTION_SET_ATTRIBUTE)) {
                String art = getArtifactIdentifier(source);

                logger.info("Set attribute for collection '" + identifier + "'");

                Document attr = getCollectionAttribute(source);

                out = db.setCollectionAttribute(identifier, meta, attr);
            }
            else if (action.equals(ACTION_SET_ITEM_ATTRIBUTE)) {
                String art = getArtifactIdentifier(source);

                logger.info("Set attribute for artifact '" + art + "'");
                out = db.setCollectionItemAttribute(identifier, art, source, meta);
            }
            else if (action.equals(ACTION_GET_ATTRIBUTE)) {
                String art = getArtifactIdentifier(source);

                logger.info("Retrieve attribute of collection '" + identifier + "'");
                out = db.getCollectionAttribute(identifier, meta);
            }
            else if (action.equals(ACTION_GET_ITEM_ATTRIBUTE)) {
                String art = getArtifactIdentifier(source);

                logger.info("Retrieve attribute of artifact '" + art + "'");
                out = db.getCollectionItemAttribute(identifier, art, meta);
            }
            else if (action.equals(ACTION_SET_TTL)) {
                out = db.setCollectionTTL(identifier, source, meta);
            }
            else if (action.equals(ACTION_SET_NAME)) {
                out = db.setCollectionName(identifier, source, meta);
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


    @Override
    protected Representation innerPost(Representation requestRepr) {
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

        String action = XMLUtils.xpathString(
            input, XPATH_ACTION, ArtifactNamespaceContext.INSTANCE);

        if (action == null || action.length() == 0) {
            Response response = getResponse();
            response.setStatus(
                Status.CLIENT_ERROR_BAD_REQUEST, NO_ACTION_MSG);
            return new EmptyRepresentation();
        }

        Request request = getRequest();

        String identifier = (String) request.getAttributes().get("uuid");

        ArtifactDatabase db = getArtifactDatabase();

        return dispatch(identifier, action, input, db);
    }


    /**
     * Retrieves the identifier of the artifact used in the action.
     *
     * @param source The incoming document that describes the operation.
     *
     * @return the uuid of the artifact described in the document.
     */
    protected String getArtifactIdentifier(Document source) {
        return XMLUtils.xpathString(
            source, XPATH_ARTIFACT, ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * Returns the attribute for a collection of the incoming request document.
     *
     * @param request The request document.
     *
     * @return the contained attribute as document.
     */
    protected Document getCollectionAttribute(Document request) {
        Node attr = (Node) XMLUtils.xpath(
            request,
            ArtifactDatabaseImpl.XPATH_COLLECTION_ATTRIBUTE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        Document newAttr = XMLUtils.newDocument();

        if (attr == null) {
            logger.error("Collection attribute document not found!");
            return newAttr;
        }

        newAttr.appendChild(newAttr.importNode(attr, true));

        return newAttr;
    }
}
