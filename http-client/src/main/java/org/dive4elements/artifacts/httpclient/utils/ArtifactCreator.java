/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.utils;

import org.w3c.dom.Document;


/**
 * This interface is used to create new implementation dependend instances of an
 * <i>Artifact</i>.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface ArtifactCreator {

    /**
     * This method should return a new Artifact object.
     *
     * @param doc A document that describes the artifact.
     *
     * @return an implementation dependend instance of an <i>Artifact</i>.
     */
    public Object create(Document doc);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
