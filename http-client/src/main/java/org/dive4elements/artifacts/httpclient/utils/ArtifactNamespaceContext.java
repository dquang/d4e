/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.httpclient.utils;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * The namespacecontext object used in xml documents retrieved by the artifact
 * server.
 *
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class ArtifactNamespaceContext
implements   NamespaceContext
{
    /**
     * The URI of the namespace of the artifacts.
     */
    public final static String NAMESPACE_URI = "http://www.intevation.de/2009/artifacts";

    /**
     * The XML prefix for the artifacts namespace.
     */
    public final static String NAMESPACE_PREFIX = "art";

    /**
     * The singleton instance of this <code>NamespaceContext</code>
     */
    public static final ArtifactNamespaceContext INSTANCE =
        new ArtifactNamespaceContext();

    /**
     * Constructor
     */
    public ArtifactNamespaceContext() {
    }

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

    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
