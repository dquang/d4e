/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/** Create ArtifactCollections. */
public interface ArtifactCollectionFactory
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
     * @return a new {@linkplain org.dive4elements.artifacts.ArtifactCollection ArtifactCollection}
     */
    ArtifactCollection createCollection(
        String   identifier,
        String   name,
        Date     creationTime,
        long     ttl,
        Document data,
        Object   context);

    /**
     * Setup the factory with a given configuration
     * @param config the configuration
     * @param factoryNode the ConfigurationNode of this Factory
     */
    void setup(Document config, Node factoryNode);

    /**
     * Tells how long a collection should survive if it is
     * not touched. This is put in the factory because
     * life time is nothing a collection should handle it self.
     * This method is only called once directly after the
     * artifact is created.
     * @param artifact The artifact to be rated.
     * @param context  The global context.
     * @return time to live in ms. null means eternal.
     */
    Long timeToLiveUntouched(ArtifactCollection collection, Object context);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
