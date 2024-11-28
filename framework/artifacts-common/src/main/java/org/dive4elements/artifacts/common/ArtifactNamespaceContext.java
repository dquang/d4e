/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.common;

import java.util.Iterator;

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

    /**
     * The default constructor.
     */
    public ArtifactNamespaceContext() {
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(String)
     * @param prefix The prefix
     * @return The corresponing URI
     */
    public String getNamespaceURI(String prefix) {

        if (prefix == null) {
            throw new NullPointerException("Null prefix");
        }

        if (NAMESPACE_PREFIX.equals(prefix)) {
            return NAMESPACE_URI;
        }

        if ("xml".equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }

        return XMLConstants.NULL_NS_URI;
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefix(String)
     * @param uri The URI
     * @return nothing.
     * @throws java.lang.UnsupportedOperationException
     */
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     * @param uri The URI
     * @return nothing
     * @throws java.lang.UnsupportedOperationException
     */
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
