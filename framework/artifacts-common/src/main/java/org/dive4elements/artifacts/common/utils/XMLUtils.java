/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.common.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
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
import javax.xml.xpath.XPathVariableResolver;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;


/**
 * Some helper functions to ease work with XML concering namespaces, XPATH
 * and so on.
 *  @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public final class XMLUtils
{
    /** W3C URL of XForms. */
    public static final String XFORM_URL    = "http://www.w3.org/2002/xforms";

    /** W3C prefix of XForms. */
    public static final String XFORM_PREFIX = "xform";

    /** Logger for this class. */
    private static Logger logger = LogManager.getLogger(XMLUtils.class);

    private XMLUtils() {
    }

    /**
     * Helper class to generate elements and attributes with
     * namespaces.
     */
    public static class ElementCreator
    {
        /** Owner document of the elements to be created. */
        protected Document document;

        /** Namespace to be used. */
        protected String   ns;

        /** Prefix to be used. */
        protected String   prefix;

        /**
         * Constructor to create an element/attribute creator
         * with a given namespace and namespace prefix using a
         * given owner document.
         * @param document The owning document
         * @param ns       The namespace
         * @param prefix   The namespace prefix
         */
        public ElementCreator(Document document, String ns, String prefix) {
            this.document = document;
            this.ns       = ns;
            this.prefix   = prefix;
        }

        /**
         * Creates a new element using the owning document with
         * the this creators namespace and namespace prefix.
         * @param name The name of the element
         * @return     The new element
         */
        public Element create(String name) {
            Element element = document.createElementNS(ns, name);
            element.setPrefix(prefix);
            return element;
        }

        /**
         * Adds a new attribute and its value to a given element.
         * It does not set the namespace prefix.
         * @param element The element to add the attribute to
         * @param name    The name of the attribute
         * @param value   The value of the attribute
         */
        public void addAttr(Element element, String name, String value) {
            addAttr(element, name, value, false);
        }

        /**
         * Adds a new attribute and its value to a given element.
         * If the namespace prefix is used is decided by the 'addPrefix' flag.
         * @param element The element to add the attribute to
         * @param name    The name of the attribute
         * @param value   The value of the attribute
         * @param addPrefix If true the creators namespace prefix is
         * set on the attribute.
         */
        public void addAttr(
            Element element,
            String  name,
            String  value,
            boolean addPrefix
        ) {
            if (addPrefix) {
                Attr attr = document.createAttributeNS(ns, name);
                attr.setValue(value);
                attr.setPrefix(prefix);

                element.setAttributeNode(attr);
            }
            else {
                element.setAttribute(name, value);
            }
        }
    } // class ElementCreator

    /**
     * Resolver for entities in artifacts configuration.
     */
    public static final EntityResolver CONF_RESOLVER = new EntityResolver() {
        @Override
        public InputSource resolveEntity(
            String publicId,
            String systemId
        ) throws SAXException, IOException {
            return new InputSource(
                new FileReader(Config.replaceConfigDir(systemId)));
        }
    };


    /**
     * Creates a new XML document
     * @return the new XML document ot null if something went wrong during
     * creation.
     */
    public static final Document newDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            return factory.newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException pce) {
            logger.error(pce.getLocalizedMessage(), pce);
        }
        return null;
    }


    /**
     * Create xml/string representation of element (nested in otherwise empty
     * document).
     * @param element element to inspect in string.
     * @return string with xml representation of element.
     */
    public final static String toString(Node node) {
        Document doc = newDocument();
        doc.appendChild(doc.importNode(node,true));
        return toString(doc);
    }


    /**
     * Loads a XML document namespace aware from a file
     * @param file The file to load.
     * @return the XML document or null if something went wrong
     * during loading.
     */
    public static final Document parseDocument(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return parseDocument(inputStream);
        }
        catch (IOException ioe) {
            logger.error(ioe.getLocalizedMessage(), ioe);
        }
        finally {
            if (inputStream != null) {
                try { inputStream.close(); }
                catch (IOException ioe) {}
            }
        }
        return null;
    }

    /**
     * Parses a String to a xml document.
     *
     * @param string The xml string
     * @return the XML document or null if something went wrong.
     */
    public static final Document parseDocument(String string) {
        InputStream inputStream = new ByteArrayInputStream(string.getBytes());
        return parseDocument(inputStream);
    }


    public static final Document parseDocument(InputStream inputStream) {
        return parseDocument(inputStream, Boolean.TRUE);
    }

    public static final Document parseDocument(
        InputStream inputStream,
        Boolean     namespaceAware
    ) {
        return parseDocument(
            inputStream, namespaceAware, CONF_RESOLVER);
    }

    public static final Document parseDocument(
        File           file,
        Boolean        namespaceAware,
        EntityResolver entityResolver
    ) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return parseDocument(inputStream, namespaceAware, entityResolver);
        }
        catch (IOException ioe) {
            logger.error(ioe.getLocalizedMessage(), ioe);
        }
        finally {
            if (inputStream != null) {
                try { inputStream.close(); }
                catch (IOException ioe) {}
            }
        }
        return null;
    }

    public static final Document parseDocument(
        InputStream    inputStream,
        Boolean        namespaceAware,
        EntityResolver entityResolver
    ) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        if (namespaceAware != null) {
            factory.setNamespaceAware(namespaceAware.booleanValue());
        }

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);
            return builder.parse(inputStream);
        }
        catch (ParserConfigurationException pce) {
            logger.error(pce.getLocalizedMessage(), pce);
        }
        catch (SAXException se) {
            logger.error(se.getLocalizedMessage(), se);
        }
        catch (IOException ioe) {
            logger.error(ioe.getLocalizedMessage(), ioe);
        }
        return null;
    }

    /**
     * Creates a new XPath without a namespace context.
     * @return the new XPath.
     */
    public static final XPath newXPath() {
        return newXPath(null, null);
    }

    /**
     * Creates a new XPath with a given namespace context.
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @return The new XPath
     */
    public static final XPath newXPath(
        NamespaceContext      namespaceContext,
        XPathVariableResolver resolver)
    {
        XPathFactory factory = XPathFactory.newInstance();
        XPath        xpath   = factory.newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }

        if (resolver != null) {
            xpath.setXPathVariableResolver(resolver);
        }
        return xpath;
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type. No namespace context is used.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param returnTyp The type of the result.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
        Object root,
        String query,
        QName  returnTyp
    ) {
        return xpath(root, query, returnTyp, null);
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a string. A given namespace context is used.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @return The result of the query or null if something went wrong
     * during XPath evaluation.
     */
    public static final String xpathString(
        Object root, String query, NamespaceContext namespaceContext
    ) {
        return (String)xpath(
            root, query, XPathConstants.STRING, namespaceContext);
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type. Optionally a namespace context is used.
     * @param root The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param returnType The type of the result.
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
        Object           root,
        String           query,
        QName            returnType,
        NamespaceContext namespaceContext
    ) {
        return xpath(root, query, returnType, namespaceContext, null);
    }

    public static final Object xpath(
        Object           root,
        String           query,
        QName            returnType,
        NamespaceContext namespaceContext,
        Map<String, String> variables)
    {
        if (root == null) {
            return null;
        }

        XPathVariableResolver resolver = variables != null
            ? new MapXPathVariableResolver(variables)
            : null;

        try {
            XPath xpath = newXPath(namespaceContext, resolver);
            if (xpath != null) {
                return xpath.evaluate(query, root, returnType);
            }
        }
        catch (XPathExpressionException xpee) {
            logger.error(xpee.getLocalizedMessage(), xpee);
        }

        return null;
    }

    /**
     * Streams out an XML document to a given output stream.
     * @param document The document to be streamed out.
     * @param out      The output stream to be used.
     * @return true if operation succeeded else false.
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

    public static byte [] toByteArray(Document document) {
        return toByteArray(document, false);
    }

    /**
     * Transforms an XML document into a byte array.
     * @param document The document to be streamed out.
     * @param compress The document should be compressed, too.
     * @return the byte array or null if operation failed or
     * document is null.
     */
    public static byte [] toByteArray(Document document, boolean compress) {
        if (document != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStream out = compress
                    ? new GZIPOutputStream(baos)
                    : baos;
                boolean success = toStream(document, out);
                out.flush();
                out.close();
                return success
                    ? baos.toByteArray()
                    : null;
            }
            catch (IOException ioe) {
                logger.error(ioe);
            }
        }
        return null;
    }

    public static Document fromByteArray(byte [] data) {
        return fromByteArray(data, false);
    }

    public static Document fromByteArray(byte [] data, boolean decompress) {
        if (data != null) {
            InputStream in = new ByteArrayInputStream(data);
            try {
                if (decompress) {
                    in = new GZIPInputStream(in);
                }
                return parseDocument(in);
            }
            catch (IOException ioe) {
                logger.error(ioe);
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                    logger.error(ioe);
                }
            }
        }
        return null;
    }

    private static class BuildResult {
        List<Node>          children;
        Map<String, String> attributes;
        BuildResult() {
            children   = new ArrayList<Node>();
            attributes = new LinkedHashMap<String, String>();
        }

        void setAttributes(Element element) {
            for (Map.Entry<String, String> entry: attributes.entrySet()) {
                element.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        void finish(Element element) {
            setAttributes(element);
            for (Node child: children) {
                element.appendChild(child);
            }
        }

        void add(Node node) {
            children.add(node);
        }

        void add(String key, Object value) {
            attributes.put(key, value != null ? value.toString() : "null");
        }

        int numChildren() {
            return children.size();
        }

        Node firstChild() {
            return children.get(0);
        }
    } // class BuildResult

    private static BuildResult recursiveBuild(
        List     list,
        Document document
    ) {
        BuildResult result = new BuildResult();
        for (Object entry: list) {
            if (entry instanceof Map) {
                BuildResult subResult = recursiveBuild(
                    (Map<String, Object>)entry, document);
                if (subResult.numChildren() == 1) {
                    result.add(subResult.firstChild());
                }
                else {
                    Element element = document.createElement("map");
                    subResult.finish(element);
                    result.add(element);
                }
            }
            else if (entry instanceof List) {
                Element element = document.createElement("list");
                BuildResult subResult = recursiveBuild((List)entry, document);
                subResult.finish(element);
                result.add(element);
            }
            else {
                Element element = document.createElement("entry");
                element.setAttribute(
                    "value",
                    entry != null ? entry.toString() : "null");
            }
        }
        return result;
    }

    private static BuildResult recursiveBuild(
        Map<String, Object> map,
        Document            document
    ) {
        BuildResult result = new BuildResult();

        List<Node> nodes = new ArrayList<Node>();
        for (Map.Entry<String, Object> entry: map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                Element element = document.createElement(entry.getKey());
                BuildResult subResult = recursiveBuild(
                    (Map<String, Object>)value, document);
                subResult.finish(element);
                result.add(element);
            }
            else if (value instanceof List) {
                Element element = document.createElement(entry.getKey());
                BuildResult subResult = recursiveBuild((List)value, document);
                subResult.finish(element);
                result.add(element);
            }
            else {
                result.add(entry.getKey(), value);
            }
        }
        return result;
    }

    public static Document jsonToXML(String input) {
        Document document = newDocument();

        if (document == null) {
            return null;
        }

        Map<String, Object> map;
        try {
            map = JSON.parse(input);
        }
        catch (IOException ioe) {
            logger.error(ioe);
            return null;
        }

        BuildResult roots = recursiveBuild(map, document);

        int N = roots.children.size();

        if (N == 1) {
            document.appendChild(roots.children.get(0));
        }
        else if (N > 1) {
            Node root = document.createElement("root");
            for (int i = 0; i < N; ++i) {
                root.appendChild(roots.children.get(i));
            }
            document.appendChild(root);
        }

        return document;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
