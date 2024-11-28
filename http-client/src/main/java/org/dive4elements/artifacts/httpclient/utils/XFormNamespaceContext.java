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
 * @author <a href="mailto:ingo.weinzierl@intevation">Ingo Weinzierl</a>
 */
public class XFormNamespaceContext
implements   NamespaceContext
{
    public final static String NAMESPACE_URI = "http://www.w3.org/2002/xforms";

    public final static String NAMESPACE_PREFIX = "xform";

    public static final XFormNamespaceContext INSTANCE =
        new XFormNamespaceContext();

    /**
     * Constructor
     */
    public XFormNamespaceContext() {
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
