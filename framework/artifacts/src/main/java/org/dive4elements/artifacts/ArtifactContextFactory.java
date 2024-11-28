/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import org.w3c.dom.Document;

/**
 * Interface of a factory that produces a global artifact context in the artifact data base.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface ArtifactContextFactory
{
    /**
     * Creates a global context given a configuration in the artifact data base.
     * @param config the configuration.
     * @return The global context.
     *   {@link org.dive4elements.artifacts.ArtifactFactory#createArtifact(String, Object, Document) createArtifact()}
     *   {@link org.dive4elements.artifacts.Artifact Artifact}
     */
    GlobalContext createArtifactContext(Document config);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
