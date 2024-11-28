/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.xml.XMLConstants;

import javax.xml.namespace.NamespaceContext;

/**
 * The namespace used in artifact documents.
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class ArtifactNamespaceContext
implements   NamespaceContext
{
    /**
     * The URI of the namespace of the artifacts.
     */
    public final static String NAMESPACE_URI =
        "http://www.intevation.de/2009/artifacts";

    /**
     * The XML prefix for the artifacts namespace.
     */
    public final static String NAMESPACE_PREFIX = "art";

    /**
     * Final instance to be easily used to avoid creation
     * of instances.
     */
    public static final ArtifactNamespaceContext INSTANCE =
        new ArtifactNamespaceContext();

    protected Map<String, String> map;

    /**
     * The default constructor.
     */
    public ArtifactNamespaceContext() {
        map = new HashMap<String, String>();
        map.put(
            XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        map.put(
            XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.DEFAULT_NS_PREFIX);
        map.put(
            XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
        map.put(
            NAMESPACE_PREFIX, NAMESPACE_URI);
    }

    public void add(String prefix, String uri) {
        map.put(prefix, uri);
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(String)
     * @param prefix The prefix
     * @return The corresponing URI
     */
    @Override
    public String getNamespaceURI(String prefix) {

        if (prefix == null) {
            throw new IllegalArgumentException("Null prefix");
        }

        String namespace = map.get(prefix);

        return namespace != null ? namespace : XMLConstants.NULL_NS_URI;
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefix(String)
     * @param uri The URI
     */
    @Override
    public String getPrefix(String uri) {

        if (uri == null) {
            throw new IllegalArgumentException("Null uri");
        }

        for (Map.Entry<String, String> entry: map.entrySet()) {
            if (entry.getValue().equals(uri)) {
                return entry.getKey();
            }
        }

        return XMLConstants.DEFAULT_NS_PREFIX;
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     * @param uri The URI
     */
    @Override
    public Iterator getPrefixes(String uri) {
        ArrayList<String> results = new ArrayList<String>();
        for (Map.Entry<String, String> entry: map.entrySet()) {
            if (entry.getValue().equals(uri)) {
                results.add(entry.getKey());
            }
        }
        return results.iterator();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
