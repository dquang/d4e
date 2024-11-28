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
import org.w3c.dom.Node;


/**
 * Interface of an artifact producing factory.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface ArtifactFactory extends Serializable
{
    /**
     * The short name of this factory.
     * @return the name of this factory.
     */
    String getName();

    /**
     * Description of this factory.
     * @return description of the factory.
     */
    String getDescription();

    /**
     * Create a new artifact of certain type, given a general purpose context and
     * an identifier.
     * @param context a context from the ArtifactDatabase.
     * @param identifier unique identifer for the new artifact
     * @param data  the data containing more details for the setup of an Artifact.
     * @return a new {@linkplain org.dive4elements.artifacts.Artifact Artifact}
     */
    Artifact createArtifact(
        String        identifier,
        GlobalContext context,
        CallMeta      callMeta,
        Document      data);

    /**
     * Setup the factory with a given configuration
     * @param config the configuration
     * @param factoryNode the ConfigurationNode of this Factory
     */
    void setup(Document config, Node factoryNode);

    /**
     * Tells how long an artifact should survive if it is
     * not touched. This is put in the factory because
     * life time is nothing an artifact should handle it self.
     * This method is only called once directly after the
     * artifact is created.
     * @param artifact The artifact to be rated.
     * @param context  The global context.
     * @return time to live in ms. null means eternal.
     */
    Long timeToLiveUntouched(Artifact artifact, Object context);

    /**
     * Returns the serializer used to store the artifacts.
     * @return The Serializer
     */
    ArtifactSerializer getSerializer();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
