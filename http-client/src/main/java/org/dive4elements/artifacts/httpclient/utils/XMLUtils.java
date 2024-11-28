/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.httpclient.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class provides many helper-Methods for handling different kinds of
 * XML-stuff.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class XMLUtils {

    /**
     * the logger, used to log exceptions and additonaly information
     */
    private static Logger logger = LogManager.getLogger(XMLUtils.class);

    /**
     * Constructor
     */
    public XMLUtils() {
    }

    /**
     * Class which could be used to create XML-Elements
     * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
     *
     */
    public static class ElementCreator {

        /**
         * The document the elements should be placed in.
         */
        protected Document document;

        /**
         * The namespace that should be used.
         */
        protected String ns;

        /**
         * The prefix of the namespace that should be used.
         */
        protected String prefix;

        /**
         * Constructor
         * @param document the document the elements should be placed in
         * @param ns the namespace that should be used
         * @param prefix the prefix of the namespace that should be used
         */
        public ElementCreator(Document document, String ns, String prefix) {
            this.document = document;
            this.ns = ns;
            this.prefix = prefix;
        }

        /**
         * Creates a new element using the given name.
         * @param name the name of the new element.
         * @return the new element
         */
        public Element create(String name) {
            Element element = document.createElementNS(ns, name);
            element.setPrefix(prefix);
            return element;
        }

        /**
         * Adds a new attribute to the given element.
         * @param element the element where the attribute should be placed in.
         * @param name the name of the attribute
         * @param value the value of the attribute
         */
        public void addAttr(Element element, String name, String value) {
            Attr attr = document.createAttributeNS(ns, name);
            attr.setValue(value);
            attr.setPrefix(prefix);
            element.setAttributeNode(attr);
        }
    } // class ElementCreator

    /**
     * Creates a new document.
     * @return the new document
     */
    public static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException pce) {
            logger.error(pce.getLocalizedMessage(), pce);
        }
        return null;
    }

    /**
     * Creates a new <code>XPath</code>-expression
     * @return the new <code>XPath</code>-expression
     */
    public static XPath newXPath() {
        return newXPath(null);
    }

    /**
     * Creates a new <code>XPath</code>-expression
     * @param namespaceContext the namespacecontext that should be used.
     * @return the new <code>XPath</code>-expression
     */
    public static XPath newXPath(NamespaceContext namespaceContext) {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }
        return xpath;
    }

    /**
     * Fetch the value of an element or attribute from the given resource
     * using the query.
     * @param root the source where the value should be fetch from
     * @param query the query that should be used to fetch the value
     * @param namespaceContext the namespacecontext that must match to
     *                         fetch the value.
     * @return the value
     */
    public static final String xpathString(Object root, String query,
                                    NamespaceContext namespaceContext) {
        return (String) xpath(root, query, XPathConstants.STRING,
                namespaceContext);
    }

    /**
     * Fetch the object rom the given resource  using the query.
     * @param root the source where the value should be fetch from
     * @param query the query that should be used to fetch the object
     * @param returnType the Type that must be used to return the object,
     * @param namespaceContext the namespacecontext that must match to
     *                         fetch the object.
     * @return the value
     */
    public static final Object xpath(Object root, String query,
                                     QName returnType,
                                     NamespaceContext namespaceContext) {
        if (root == null) {
            return null;
        }
        try {
            XPath xpath = XMLUtils.newXPath(namespaceContext);
            if (xpath != null) {
                return xpath.evaluate(query, root, returnType);
            }
        } catch (XPathExpressionException xpee) {
            logger.error(xpee.getLocalizedMessage(), xpee);
        }
        return null;
    }

    /**
     * Fetch the object rom the given resource  using the query
     * and the default <code>ArtifactNamespaceContext</code>
     * @param root the source where the value should be fetch from
     * @param query the query that should be used to fetch the object
     * @param returnType the Type that must be used to return the object
     * @return the value
     */
    public static Object getXPath(Object root, String query, QName returnType) {
        return getXPath(root,query,returnType,ArtifactNamespaceContext.INSTANCE);
    }

    /**
     * Fetch the object rom the given resource  using the query
     * and the default <code>ArtifactNamespaceContext</code>
     * @param root the source where the value should be fetch from
     * @param query the query that should be used to fetch the object
     * @param returnType the Type that must be used to return the object.
     * @param context the namespacecontext that must match to
     *                         fetch the object.
     * @return the value
     */
    public static Object getXPath(
        Object root, String query, QName returnType, NamespaceContext context
    ) {
        return xpath(root, query, returnType, context);
    }

    /**
     * Fetch a Nodeset value from a XML-Fragment or XML-Document using the
     * given query.
     * @param root the source where the String should be fetched from
     * @param query the query that should be used,
     * @return the Nodeset fetched from the source
     */
    public static NodeList getNodeSetXPath(Object root, String query) {
        return (NodeList) getXPath(root, query, XPathConstants.NODESET);
    }

    /**
     * Fetch a Node from a XML-Fragment or XML-Document using the
     * given query.
     * @param root the source where the Node should be fetched from
     * @param query the query that should be used,
     * @return the Node fetched from the source
     */
    public static Node getNodeXPath(Object root, String query) {
        return (Node) getXPath(root, query, XPathConstants.NODE);
    }

    /**
     * Fetch a String value from a XML-Fragment or XML-Document using the
     * given query.
     * @param root the source where the String should be fetched from
     * @param xpath the query that should be used,
     * @return the String fetched from the source
     */
    public static String getStringXPath(Object root, String xpath) {
        return getStringXPath(root, xpath, null);
    }

    /**
     * Fetch a String value from a XML-Fragment or XML-Document using the
     * given query.
     * @param root the source where the String should be fetched from
     * @param query the query that should be used,
     * @param def the default-value that will be returned id no value was found
     * @return the String fetched from the source
     */
    public static String getStringXPath(Object root, String query, String def) {
        String s = (String) getXPath(root, query, XPathConstants.STRING);
        return s == null || s.length() == 0 ? def : s;
    }

    /**
     * Reads an XML-document from a given <code>InputStream</code>
     * @param inputStream the <code>InputStream</code> where the document
     *                    should be read from
     * @return the document that could be read.
     */
    public static Document readDocument(InputStream inputStream) {
        Document returnValue = null;
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            returnValue = docBuilder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            logger.error(e, e);
        } catch (SAXException e) {
            logger.error(e, e);
        } catch (IOException e) {
            logger.error(e, e);
        }
        return returnValue;
    }

    /**
     * Writes a given Document to an <code>OutputStream</code>
     * @param document the document that should be written
     * @param out the stream where the document should be written to,
     * @return true if it was successful, false if not.
     */
    public static boolean toStream(Document document, OutputStream out) {
        try {
            Transformer transformer =
                TransformerFactory.newInstance().newTransformer();
            DOMSource    source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            return true;
        }
        catch (TransformerConfigurationException tce) {
            logger.error(tce.getLocalizedMessage(), tce);
        }
        catch (TransformerFactoryConfigurationError tfce) {
            logger.error(tfce.getLocalizedMessage(), tfce);
        }
        catch (TransformerException te) {
            logger.error(te.getLocalizedMessage(), te);
        }
        return false;
    }

    public static String toString(Document document) {
        try {
            Transformer transformer =
                TransformerFactory.newInstance().newTransformer();
            DOMSource    source = new DOMSource(document);
            StringWriter out    = new StringWriter();
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.flush();
            return out.toString();
        }
        catch (TransformerConfigurationException tce) {
            logger.error(tce.getLocalizedMessage(), tce);
        }
        catch (TransformerFactoryConfigurationError tfce) {
            logger.error(tfce.getLocalizedMessage(), tfce);
        }
        catch (TransformerException te) {
            logger.error(te.getLocalizedMessage(), te);
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
