/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.common.utils;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;


/**
 * This class provides methods that help creating the artifact protocol
 * documents DESCRIBE, FEED, ADVANCE and OUT.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ClientProtocolUtils {

    /** The XPath to the current state in the DESCRIBE document. */
    public static final String XPATH_CURRENT_STATE = "/art:result/art:state";

    /** The XPath to the static UI part in the DESCRIBE document. */
    public static final String XPATH_STATIC  = "/art:result/art:ui/art:static";

    /** The XPath to the dynamic UI part in the DESCRIBE document. */
    public static final String XPATH_DYNAMIC = "/art:result/art:ui/art:dynamic";

    /** The XPath to the reachable states part in the DESCRIBE document. */
    public static final String XPATH_STATES  =
        "/art:result/art:reachable-states";

    /** The XPath to the output modes in the DESCRIBE document. */
    public static final String XPATH_OUTPUT_MODES  =
        "/art:result/art:outputmodes/art:output";

    /** The XPath to the select node relative to the dynamic UI node in the
     * DESCRIBE document. */
    public static final String XPATH_DATA_SELECT = "art:select";

    /** The XPath to the choices nodes relative to the select node in the
     * DESCRIBE document. */
    public static final String XPATH_DATA_ITEMS = "art:choices/art:item";

    /** The XPath that points to the min value of a range.*/
    public static final String XPATH_MIN_NODE = "art:min/@art:value";

    /** The XPath that points to the max value of a range.*/
    public static final String XPATH_MAX_NODE = "art:max/@art:value";

    /** The XPath that points to the default min value of a range.*/
    public static final String XPATH_DEF_MIN = "art:min/@art:default";

    /** The XPath that points to the default max value of a range.*/
    public static final String XPATH_DEF_MAX = "art:max/@art:default";

    /** The XPath to a label in the artifact's DESCRIBE document. */
    public static final String XPATH_LABEL = "art:label/text()";

    /** The XPath to a value in the artifact's DESCRIBE document. */
    public static final String XPATH_VALUE = "art:value/text()";


    /**
     * It should not be necessary to create instances of this class.
     */
    private ClientProtocolUtils() {
    }


    /**
     * This method creates a new CREATE document.
     *
     * @return the CREATE document.
     */
    public static Document newCreateDocument(String factory) {
        return newCreateDocument(factory, null);
    }


    /**
     * This method creates a new CREATE document.
     *
     * @return the CREATE document.
     */
    public static Document newCreateDocument(String factory, String uuid) {
        return newCreateDocument(factory, uuid, null);
    }

    public static Document newCreateDocument(
        String  factory,
        String  uuid,
        String  ids
    ) {
        return newCreateDocument(factory, uuid, ids, null);
    }

    /**
     * This method creates a new CREATE document.
     *
     * @return the CREATE document.
     */
    public static Document newCreateDocument(
        String         factory,
        String         uuid,
        String         ids,
        CreationFilter filter
    ) {
        return newCreateDocument(factory, uuid, ids, filter, null);
    }

    public static Document newCreateDocument(
        String         factory,
        String         uuid,
        String         ids,
        CreationFilter filter,
        String         targetOut
    ) {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = cr.create("action");
        Element type   = cr.create("type");
        Element fac    = cr.create("factory");

        type.setAttribute("name", "create");
        fac.setAttribute("name", factory);

        action.appendChild(type);
        action.appendChild(fac);

        if (uuid != null) {
            Element templ = cr.create("template");
            templ.setAttribute("uuid", uuid);
            action.appendChild(templ);
        }

        if (ids != null) {
            Element id = cr.create("ids");
            id.setAttribute("value", ids);
            action.appendChild(id);
        }

        if (filter != null) {
            action.appendChild(filter.toXML(cr));
        }

        if (targetOut != null) {
            Element to = cr.create("target_out");
            to.setAttribute("value", targetOut);
            action.appendChild(to);
        }

        doc.appendChild(action);

        return doc;
    }


    /**
     * This method creates a new FEED document.
     *
     * @param theUuid The identifier of the artifact.
     * @param theHash The hash of the artifact.
     * @param theData An array that contains key/value pairs that represent the
     * data that should be included in the FEED document.
     *
     * @return the FEED document.
     */
    public static Document newFeedDocument(
        String     theUuid,
        String     theHash,
        String[][] theData)
    {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = cr.create("action");
        Element type   = cr.create("type");
        Element uuid   = cr.create("uuid");
        Element hash   = cr.create("hash");
        Element data   = cr.create("data");

        // XXX It is not nice that the type has no attribute namespace, but to
        // be backward compatible, we don't change this now.
        cr.addAttr(type, "name", "feed", false);
        cr.addAttr(uuid, "value", theUuid, true);
        cr.addAttr(hash, "value", theHash, true);

        for (String[] kvp: theData) {
            Element input = cr.create("input");
            cr.addAttr(input, "name", kvp[0], true);
            cr.addAttr(input, "value", kvp[1], true);

            data.appendChild(input);
        }

        action.appendChild(type);
        action.appendChild(uuid);
        action.appendChild(hash);
        action.appendChild(data);

        doc.appendChild(action);

        return doc;
    }


    /**
     * This method creates a new DESCRIBE document.
     *
     * @param theUuid The identifier of the artifact.
     * @param theHash The hash of the artifact.
     * @param ui If true, the UI part is included.
     *
     * @return the DESCRIBE document.
     */
    public static Document newDescribeDocument(
        String  theUuid,
        String  theHash,
        boolean incUI)
    {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = cr.create("action");
        Element type   = cr.create("type");
        Element uuid   = cr.create("uuid");
        Element hash   = cr.create("hash");
        Element ui     = cr.create("include-ui");

        // XXX It is not nice that the type has no attribute namespace, but to
        // be backward compatible, we don't change this now.
        cr.addAttr(type, "name", "describe", false);
        cr.addAttr(uuid, "value", theUuid, true);
        cr.addAttr(hash, "value", theHash, true);

        ui.setTextContent(incUI ? "true" : "false");

        action.appendChild(type);
        action.appendChild(uuid);
        action.appendChild(hash);
        action.appendChild(ui);

        doc.appendChild(action);

        return doc;
    }


    /**
     * This method creates a new ADVANCE document.
     *
     * @param theUuid The identifier of the artifact.
     * @param theHash The hash of the artifact.
     * @param theTarget The target state identifier.
     *
     * @return the ADVANCE document.
     */
    public static Document newAdvanceDocument(
        String theUuid,
        String theHash,
        String theTarget)
    {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = cr.create("action");
        Element type   = cr.create("type");
        Element uuid   = cr.create("uuid");
        Element hash   = cr.create("hash");
        Element target = cr.create("target");

        // XXX It is not nice that the type has no attribute namespace, but to
        // be backward compatible, we don't change this now.
        cr.addAttr(type, "name", "advance", false);
        cr.addAttr(uuid, "value", theUuid, true);
        cr.addAttr(hash, "value", theHash, true);
        cr.addAttr(target, "name", theTarget, true);

        action.appendChild(type);
        action.appendChild(uuid);
        action.appendChild(hash);
        action.appendChild(target);

        doc.appendChild(action);

        return doc;
    }


    /**
     * This method creates a new document that is used to create new artifact
     * collections in the artifact server.
     *
     * @param name <b>Optional</b> name of the collection.
     *
     * @return the document to create new collections.
     */
    public static Document newCreateCollectionDocument(String name) {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action     = cr.create("action");
        Element type       = cr.create("type");
        Element collection = cr.create("collection");
        Element attribute  = cr.create("attribute");

        cr.addAttr(type, "name", "create");
        cr.addAttr(collection, "name", name != null ? name : "");

        action.appendChild(type);
        type.appendChild(collection);
        collection.appendChild(attribute);

        doc.appendChild(action);

        return doc;
    }


    /**
     * This method creates a new Document that is used to add an artifact to a
     * collection in the artifact server.
     *
     * @param artId The identifier of the artifact that should be added.
     * @param attr A document that contains attributes for the artifact's
     * life in the collection.
     *
     * @return the document to add an artifact into a collection.
     */
    public static Document newAddArtifactDocument(String artId, Document attr) {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action    = cr.create("action");
        Element type      = cr.create("type");
        Element artifact  = cr.create("artifact");
        Element attribute = cr.create("attribute");

        cr.addAttr(artifact, "uuid", artId);
        cr.addAttr(type, "name", "addartifact");

        if (attr != null) {
            attr.appendChild(attr);
        }

        action.appendChild(type);
        type.appendChild(artifact);
        artifact.appendChild(attribute);

        doc.appendChild(action);

        return doc;
    }


    /**
     * Create a new Document that is used to remove an artifact from a
     * collection in the artifact server.
     *
     * @param artId The identifier of the artifact that should be added.
     *
     * @return the document to add an artifact into a collection.
     */
    public static Document newRemoveArtifactDocument(String artId) {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action    = cr.create("action");
        Element type      = cr.create("type");
        Element artifact  = cr.create("artifact");

        cr.addAttr(artifact, "uuid", artId);
        cr.addAttr(type, "name", "removeartifact");

        action.appendChild(type);
        type.appendChild(artifact);

        doc.appendChild(action);

        return doc;
    }


    /**
     * This method creates a new Document that is used to trigger the DESCRIBE
     * operation of a collection in the artifact server.
     *
     * @param uuid The identifier of the collection that should be described.
     *
     * @return the document to describe a collection.
     */
    public static Document newDescribeCollectionDocument(String uuid) {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action    = cr.create("action");
        Element type      = cr.create("type");
        cr.addAttr(type, "name", "describe");

        action.appendChild(type);

        doc.appendChild(action);

        return doc;
    }



    /**
     * This function builds a document that is used as request document of the
     * out() operation of Collections.
     *
     * @param uuid The identifier of the collection.
     * @param mode The name of the desired output mode.
     * @param type The name of the desired output type.
     *
     * @return the request document.
     */
    public static Document newOutCollectionDocument(
        String uuid,
        String mode,
        String type) {
        return newOutCollectionDocument(uuid, mode, type, null);
    }


    /**
     * This function builds a document that is used as request document of the
     * out() operation of Collections. The document <i>attr</i> might be used to
     * adjust some settings specific to the output.
     *
     * @param uuid The identifier of the collection.
     * @param mode The name of the desired output mode.
     * @param type The name of the desired output type.
     * @param attr A document that contains settings specific to the output.
     *
     * @return the request document.
     */
    public static Document newOutCollectionDocument(
        String   uuid,
        String   mode,
        String   type,
        Document attr)
    {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator cr = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = cr.create("action");

        cr.addAttr(action, "name", mode, true);
        cr.addAttr(action, "type", type, true);

        doc.appendChild(action);

        if (attr != null) {
            Node root = attr.getFirstChild();

            if (root != null) {
                action.appendChild(doc.importNode(root, true));
            }
        }

        return doc;
    }


    /**
     * This function creates a document that is used to set the attribute of a
     * Collection.
     *
     * @param uuid The identifier of the Collection.
     * @param attr The new attribute value for the Collection.
     *
     * @return the document that is used to set the attribute.
     */
    public static Document newSetAttributeDocument(
        String uuid,
        Document attr)
    {
        Node root = attr != null ? attr.getFirstChild() : null;

        if (root == null) {
            return null;
        }

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action     = ec.create("action");
        Element type       = ec.create("type");
        Element collection = ec.create("collection");

        ec.addAttr(type, "name", "setattribute", false);
        ec.addAttr(collection, "uuid", uuid, false);

        doc.appendChild(action);
        action.appendChild(type);
        type.appendChild(collection);

        collection.appendChild(doc.importNode(root, true));

        return doc;
    }

    /**
     * This function creates a document that is used to set the attribute of a
     * CollectionItem.
     *
     * @param uuid The identifier of the CollectionItem.
     * @param attr The new attribute value for the CollectionItem.
     *
     * @return the document that is used to set the attribute.
     */
    public static Document newSetItemAttributeDocument(
        String uuid,
        Document attr)
    {
        Node root = attr.getFirstChild();

        if (root == null) {
            return null;
        }

        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action   = ec.create("action");
        Element type     = ec.create("type");
        Element artifact = ec.create("artifact");

        ec.addAttr(type, "name", "setitemattribute");
        ec.addAttr(artifact, "uuid", uuid);

        doc.appendChild(action);
        action.appendChild(type);
        type.appendChild(artifact);

        artifact.appendChild(doc.importNode(root, true));

        return doc;
    }


    /**
     * This function creates a document that is used to set the time-to-live
     * of a collection.
     *
     * @param ttl The ttl for the Collection.
     *
     * @return the document that is used to set the time-to-live.
     */
    public static Document newSetCollectionTTLDocument(String ttl) {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = ec.create("action");
        Element type   = ec.create("type");
        Element ttlEl  = ec.create("ttl");

        ec.addAttr(type, "name", "settimetolive");
        ec.addAttr(ttlEl, "value", ttl);

        doc.appendChild(action);
        action.appendChild(type);
        type.appendChild(ttlEl);

        return doc;
    }


    /**
     * This function creates a document that is used to set the name of a
     * collection.
     *
     * @param name The name for the Collection.
     *
     * @return the document that is used to set the name of a collection.
     */
    public static Document newSetCollectionNameDocument(String name) {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = ec.create("action");
        Element type   = ec.create("type");
        Element coll   = ec.create("collection");

        ec.addAttr(type, "name", "setname");
        ec.addAttr(coll, "name", name);

        doc.appendChild(action);
        action.appendChild(type);
        type.appendChild(coll);

        return doc;
    }


    /**
     * This function creates a document that is used to delete an existing
     * collection.
     *
     * @return the document that is used to delete an existing collection.
     */
    public static Document newDeleteCollectionDocument() {
        Document doc = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element action = ec.create("action");
        Element type   = ec.create("type");

        ec.addAttr(type, "name", "delete");

        doc.appendChild(action);
        action.appendChild(type);

        return doc;
    }


    /**
     * Returns string value found by {@link XPATH_LABEL} relative to
     * <i>node</i>.
     *
     * @param node A node.
     *
     * @return the string value found by {@link XPATH_LABEL}.
     */
    public static String getLabel(Node node) {
        return (String) XMLUtils.xpath(
            node,
            XPATH_LABEL,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * Returns string value found by {@link XPATH_VALUE} relative to
     * <i>node</i>.
     *
     * @param node A node.
     *
     * @return the string value found by {@link XPATH_VALUE}.
     */
    public static String getValue(Node node) {
        return (String) XMLUtils.xpath(
            node,
            XPATH_VALUE,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * This method returns the static UI part of the artifact's DESCRIBE
     * document.
     *
     * @param description The document returned by the artifact server's
     * DESCRIBE operation.
     *
     * @return the static UI node.
     */
    public static Node getStaticUI(Document description) {
        return (Node) XMLUtils.xpath(
            description,
            XPATH_STATIC,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * This method returns the dynamic UI part of the artifact's DESCRIBE
     * document.
     *
     * @param description The document returned by the artifact server's
     * DESCRIBE operation.
     *
     * @return the dynamic UI node.
     */
    public static Node getDynamicUI(Document description) {
        return (Node) XMLUtils.xpath(
            description,
            XPATH_DYNAMIC,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * This method returns the current state node contained in the DESCRIBE
     * document.
     *
     * @param description The document returned by the artifact server's
     * DESCRIBE operation.
     *
     * @return the node containing information about the current state.
     */
    public static Node getCurrentState(Document description) {
        return (Node) XMLUtils.xpath(
            description,
            XPATH_CURRENT_STATE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * This method returns the node that contains information about the
     * reachable states of the artifact in the artifact's DESCRIBE document.
     *
     * @param description The document returned by the artifact server's
     * DESCRIBE operation.
     *
     * @return the node that contains the reachable states.
     */
    public static Node getReachableStates(Document description) {
        return (Node) XMLUtils.xpath(
            description,
            XPATH_STATES,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * This method returns the output mode nodes of the DESCRIBE document.
     *
     * @param description The document returned by the artifact server's
     * DESCRIBE operation.
     *
     * @return the node that contains the output modes.
     */
    public static NodeList getOutputModes(Document description) {
        return (NodeList) XMLUtils.xpath(
            description,
            XPATH_OUTPUT_MODES,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * Returns the node found by {@link XPATH_DATA_SELECT}.
     *
     * @param dynamicNode The dynamic UI node of the DESCRIBE document.
     *
     * @return the select node found in the dynamic UI node.
     */
    public static NodeList getSelectNode(Node dynamicNode) {
        return (NodeList) XMLUtils.xpath(
            dynamicNode,
            XPATH_DATA_SELECT,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);
    }


    /**
     * Returns the items that could be found in the <i>node</i>.
     *
     * @param node A select node.
     *
     * @return the choices nodes as node list.
     */
    public static NodeList getItemNodes(Node node) {
        return (NodeList) XMLUtils.xpath(
            node,
            XPATH_DATA_ITEMS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);
    }


    public static String getMinNode(Node parent) {
        return (String) XMLUtils.xpath(
            parent,
            XPATH_MIN_NODE,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }


    public static String getMaxNode(Node parent) {
        return (String) XMLUtils.xpath(
            parent,
            XPATH_MAX_NODE,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }


    public static String getDefMin(Node parent) {
        return (String) XMLUtils.xpath(
            parent,
            XPATH_DEF_MIN,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }


    public static String getDefMax(Node parent) {
        return (String) XMLUtils.xpath(
            parent,
            XPATH_DEF_MAX,
            XPathConstants.STRING,
            ArtifactNamespaceContext.INSTANCE);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
