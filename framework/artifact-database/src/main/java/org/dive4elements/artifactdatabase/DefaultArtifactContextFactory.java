/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.ArtifactContextFactory;
import org.dive4elements.artifacts.GlobalContext;

import org.w3c.dom.Document;

/**
 * Default implementation of the context factory.
 * Creates a new @see DefaultArtifactContext.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultArtifactContextFactory
implements   ArtifactContextFactory
{
    /**
     * Default constructor.
     */
    public DefaultArtifactContextFactory() {
    }

    public GlobalContext createArtifactContext(Document config) {
        return new DefaultArtifactContext(config);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
