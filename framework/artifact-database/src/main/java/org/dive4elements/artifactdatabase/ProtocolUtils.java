/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.ArtifactNamespaceContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.State;


/**
 * This class provides methods that help creating the artifact protocol
 * documents describe, feed, advance and out.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ProtocolUtils {

    /**
     * It should not be necessary to create instances of this class.
     */
    private ProtocolUtils() {}


    /**
     * This method creates a node that might be used for the artifact protocol.
     *
     * @param creator The ElementCreator that is used to create the node.
     * @param nodeName The node name.
     * @param attrName The names of optional attributes.
     * @param value The values for the optional attributes.
     *
     * @return the created node.
     */
    public static Element createArtNode(
        XMLUtils.ElementCreator creator,
        String nodeName, String[] attrName, String[] value)
    {
        Element typeNode = creator.create(nodeName);

        if (attrName != null && value != null) {
            for (int i = 0; i < attrName.length; i++) {
                if (i < value.length) {
                    creator.addAttr(typeNode, attrName[i], value[i], true);
                }
                else {
                    break;
                }
            }
        }

        return typeNode;
    }


    /**
     * This method creates the root node for all artifact protocol documents.
     *
     * @param creator The ElementCreator used to create new elements.
     *
     * @return the root node for the artifact protocol document.
     */
    public static Element createRootNode(XMLUtils.ElementCreator creator) {
        return createArtNode(creator, "result", null, null);
    }


    /**
     * This method appends the three necessary nodes <i>type</i>, <i>uuid</i>
     * and <i>hash</i> of the describe document to <i>root</i> node.
     *
     * @param creator The ElementCreator that is used to create new nodes.
     * @param root The root node of the describe document.
     * @param uuid The UUID of the artifact.
     * @param hash The hash if the artifact.
     */
    public static void appendDescribeHeader(
        XMLUtils.ElementCreator creator, Element root, String uuid, String hash)
    {
        root.appendChild(createArtNode(
            creator,
            "type",
            new String[] {"name"},
            new String[] {"describe"}));

        root.appendChild(createArtNode(
            creator,
            "uuid",
            new String[] {"value"},
            new String[] {uuid}));

        root.appendChild(createArtNode(
            creator,
            "hash",
            new String[] {"value"},
            new String[] {hash}));
    }


    /**
     * This method appends a node that describes the current state to
     * <i>root</i>.
     *
     * @param creator The ElementCreator used to create new elements.
     * @param root The parent node for new elements.
     * @param state The state to be appended.
     */
    public static void appendState(
        XMLUtils.ElementCreator creator, Element root, State state)
    {
        root.appendChild(createArtNode(
            creator, "state",
            new String[] { "description", "name" },
            new String[] { state.getDescription(), state.getID() }));
    }


    /**
     * This method appends a node with reachable states to <i>root</i>.
     *
     * @param creator The ElementCreator used to create new elements.
     * @param root The parent node for new elements.
     * @param states The reachable states to be appended.
     */
    public static void appendReachableStates(
        XMLUtils.ElementCreator creator,
        Element                 root,
        List<State>             states)
    {
        Element reachable = createArtNode(
            creator, "reachable-states", null, null);

        for (State s: states) {
            appendState(creator, reachable, s);
        }

        root.appendChild(reachable);
    }


    /**
     * This method appends a node for each Output in the <i>outputs</i> list to
     * <i>out</i>. Note: an output node includes its provided facets!
     *
     * @param doc The document to which to add new elements.
     * @param out The parent node for new elements.
     * @param outputs The list of reachable outputs.
     */
    public static void appendOutputModes(
        Document     doc,
        Element      out,
        List<Output> outputs)
    {
        ElementCreator creator = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        for (Output o: outputs) {
            Element newOut = createArtNode(
                creator,
                "output",
                new String[] {"name", "description", "mime-type", "type"},
                new String[] {
                    o.getName(),
                    o.getDescription(),
                    o.getMimeType(),
                    o.getType() });

            Element facets = createArtNode(creator, "facets", null, null);
            appendFacets(doc, facets, o.getFacets());

            newOut.appendChild(facets);
            out.appendChild(newOut);
        }
    }


    /**
     * This method appends a node for each Facet in the <i>facets</i> list to
     * <i>facet</i>.
     *
     * @param doc The document to wich to add new elements.
     * @param facet The root node for new elements.
     * @param facets The list of facets.
     */
    public static void appendFacets(
        Document    doc,
        Element     facet,
        List<Facet> facets)
    {
        if (facets == null || facets.size() == 0) {
            return;
        }

        ElementCreator creator = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        for (Facet f: facets) {
            Node node = f.toXML(doc);

            if (node != null) {
                facet.appendChild(node);
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
