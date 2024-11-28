/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.httpclient.objects;

/**
 * An <code>ArtifactObject</code> representing an artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 *
 */
public class Artifact {

    /**
     * The uuid of this Artifact which must be used to identify
     * the artifact at the <code>ArtifactDatabase</code>.
     */
    private String uuid = null;

    /**
     * The hash of the artifact which was send be the <code>ArtifactDatabase</code>.
     */
    private String hash = null;

    /**
     * Constructor
     * @param uuid the uuid of this Artifact which must be used to identify
     *             the artifact at the <code>ArtifactDatabase</code>
     * @param hash the hash of the artifact which was send be the
     *             <code>ArtifactDatabase</code>
     */
    public Artifact(String uuid, String hash) {
        this.uuid = uuid;
        this.hash = hash;
    }

    public String getHash() {
        return this.hash;
    }

    public String getUuid() {
        return this.uuid;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
