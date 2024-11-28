/*
 * Copyright (c) 2014 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase.state;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallMeta;
import org.w3c.dom.Document;


public interface StaticFacet
extends Facet
{
    /**
     * Setup the static facet by parsing the data document.
     *
     * @param artifact  The artifact
     * @param data      The document
     * @param meat      The call meta
     */
    public void setup(Artifact artifact, Document data, CallMeta meta);
}
