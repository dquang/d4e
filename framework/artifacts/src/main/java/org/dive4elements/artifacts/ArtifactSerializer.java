/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

/**
 * Interface to make artifact persistent.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface ArtifactSerializer
{
    /**
     * Restores an artifact from an array of bytes.
     * @param bytes the persistent representation of the artifact.
     * @return The de-serialized artifact or null if there was an error.
     */
    Artifact fromBytes(byte [] bytes);
    /**
     * Brings an artifact to a persistent form in form of a byte array.
     * @param artifact The artifact to be serialized.
     * @return the byte array representation of the artifact or null
     * if there was an error.
     */
    byte []  toBytes(Artifact artifact);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
