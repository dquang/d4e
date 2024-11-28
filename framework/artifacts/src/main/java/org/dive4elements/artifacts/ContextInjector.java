/*
 * Copyright (c) 2013 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

public interface ContextInjector {

    void setup(Element cfg);

    void injectContext(CallContext ctx, Artifact artifact, Document request);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
