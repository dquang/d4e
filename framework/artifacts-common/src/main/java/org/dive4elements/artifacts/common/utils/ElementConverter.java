/*
 * Copyright (c) 2013 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.common.utils;

import org.w3c.dom.Element;

/** Simple interface to convert an XML DOM element to somthing opaque,
 *  Useful to do some config preprocessing.
 */
public interface ElementConverter {
    Object convert(Element element);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
