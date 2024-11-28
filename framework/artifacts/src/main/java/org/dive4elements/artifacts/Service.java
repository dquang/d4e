/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import java.io.Serializable;

import org.w3c.dom.Document;

/**
 * The idea is to process some input XML document to produce an output
 * XML document.
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface Service
extends          Serializable
{
    interface Output {
        Object getData();

        String getMIMEType();
    } // interface Output

    /**
     * Processes some input XML document
     * @param data The input data
     * @param globalContext The global context of the artifact database.
     * @param callMeta The call meta contex, e.g. preferred languages.
     * @return The result.
     */
    Output process(Document data, GlobalContext globalContext, CallMeta callMeta);

    /**
     * Setup the concrete processing service. This is done at startup time
     * of the artifact database system.
     * @param factory The service factory which created this service.
     * @param globalContext The global context of the artifact database.
     */
    void setup(ServiceFactory factory, GlobalContext globalContext);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
