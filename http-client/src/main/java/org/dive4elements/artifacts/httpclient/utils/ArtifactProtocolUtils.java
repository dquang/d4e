/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.utils;

import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.httpclient.objects.Artifact;
import org.dive4elements.artifacts.httpclient.objects.ArtifactFactory;

public class ArtifactProtocolUtils {

    private static final Logger logger =
        LogManager.getLogger(ArtifactProtocolUtils.class);


    public static ArtifactFactory[] extractArtifactFactories(Document doc) {
        NodeList elements = (NodeList) XMLUtils.getXPath(
            doc,
            "/art:result/art:factories/art:factory",
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (elements == null || elements.getLength() == 0) {
            return null;
        }

        ArtifactFactory[] facs = new ArtifactFactory[elements.getLength()];

        String uri = ArtifactNamespaceContext.NAMESPACE_URI;

        for (int idx = 0; idx < facs.length; idx++) {
            Element factory = (Element)elements.item(idx);
            String desc  = factory.getAttributeNS(uri, "description");
            String name  = factory.getAttributeNS(uri, "name");

            if (name.length() != 0) {
                facs[idx] = new ArtifactFactory(name, desc);
            }
        }

        return facs;
    }


    public static Document createCreateDocument(String fis) {
        Document document = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX
        );

        Element action = creator.create("action");

        Element type = creator.create("type");
        type.setAttribute("name", "create");

        Element factory = creator.create("factory");
        factory.setAttribute("name", fis);

        action.appendChild(type);
        action.appendChild(factory);
        document.appendChild(action);

        return document;
    }


    /**
     * Returns a new artifact defined by uuid and hash values of the document
     * returned by the artifact server after creating a new artifact.
     *
     * @param document Contains information about the server-side created
     * artifact.
     * @return a new artifact object.
     */
    public static Artifact extractArtifact(Document document) {
        String uuid = XMLUtils.getStringXPath(
            document,
            "/art:result/art:uuid/@value");

        String hash = XMLUtils.getStringXPath(
            document,
            "/art:result/art:hash/@value");

        logger.info("NEW Artifact: " + uuid + " / " + hash);
        return new Artifact(uuid, hash);
    }


    private static Element createArtifactAction(
        XMLUtils.ElementCreator creator,
        Artifact                artifact,
        String                  artifactAction)
    {
        Element action = creator.create("action");

        Element type = creator.create("type");
        type.setAttribute("name", artifactAction);

        Element uuid = creator.create("uuid");
        uuid.setAttribute("value", artifact.getUuid());

        Element hash = creator.create("hash");
        hash.setAttribute("value", artifact.getHash());

        action.appendChild(type);
        action.appendChild(uuid);
        action.appendChild(hash);

        return action;
    }


    public static Document createFeedDocument(Artifact artifact, Map attr) {
        Document document = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX
        );

        Element action = createArtifactAction(creator, artifact, "feed");
        Element data   = creator.create("data");

        for (Map.Entry<String, Object> entry:
            ((Map<String, Object>)attr).entrySet()) {

            String key    = entry.getKey();
            Object values = entry.getValue();

            if (values instanceof Object[]) {
                appendInputNodes(creator, data, key, (Object[]) values);
            }
            else {
                appendInputNodes(creator, data, key, values);
            }
        }

        action.appendChild(data);
        document.appendChild(action);

        return document;
    }


    private static void appendInputNodes(
        XMLUtils.ElementCreator creator,
        Element                 root,
        String                  key,
        Object                  value)
    {
        Element input = creator.create("input");
        input.setAttribute("name", key);
        input.setAttribute("value", (String) value);
        root.appendChild(input);
    }


    private static void appendInputNodes(
        XMLUtils.ElementCreator creator,
        Element                 root,
        String                  key,
        Object[]                values)
    {
        for (Object value: values) {
            Element input = creator.create("input");
            input.setAttribute("name", key);
            input.setAttribute("value", (String) value);
            root.appendChild(input);
        }
    }


    public static Document createDescribeDocument(Artifact art, boolean ui) {
        Document document = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX
        );

        Element action    = createArtifactAction(creator, art, "describe");
        Element includeUi = creator.create("include-ui");
        includeUi.setTextContent(String.valueOf(ui));

        action.appendChild(includeUi);
        document.appendChild(action);

        return document;
    }


    public static Document createAdvanceDocument(Artifact art, String target) {
        Document document = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX
        );

        Element action    = createArtifactAction(creator, art, "advance");
        Element targetEle = creator.create("target");
        targetEle.setAttribute("name", target);

        action.appendChild(targetEle);
        document.appendChild(action);

        return document;
    }


    public static Document createChartDocument(Artifact artifact, Map opts) {
        Document document = XMLUtils.newDocument();

        XMLUtils.ElementCreator creator = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX
        );

        Element action = createArtifactAction(creator, artifact, "out");
        Element out    = creator.create("out");
        out.setAttribute("name", "chart");

        Element export = creator.create("export");
        export.setAttribute("name", "img");

        Element mimetype = creator.create("mime-type");
        export.setAttribute("value", (String) opts.get("mime-type"));

        Element params = creator.create("params");

        Element width = creator.create("input");
        width.setAttribute("name", "width");
        width.setAttribute("value", (String) opts.get("width"));

        Element height = creator.create("input");
        height.setAttribute("name", "height");
        height.setAttribute("value", (String) opts.get("height"));

        Element points = creator.create("input");
        points.setAttribute("name", "points");
        points.setAttribute("value", (String) opts.get("points"));

        params.appendChild(width);
        params.appendChild(height);
        params.appendChild(points);

        out.appendChild(export);
        out.appendChild(mimetype);
        out.appendChild(params);

        action.appendChild(out);
        document.appendChild(action);

        return document;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
