/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import java.util.Date;

public interface ArtifactCollection
extends          Serializable
{
    /**
     * Set a new identifier for this collection.
     * @param identifier New identifier for this collection.
     */
    void setIdentifier(String identifier);

    /**
     * Identify this collection.
     * @return Returns unique string to identify this collection globally.
     */
    String identifier();

    String getName();

    void setName(String name);

    Date getCreationTime();

    void setCreationTime(Date creationTime);

    long getTTL();

    void setTTL(long ttl);

    Document getAttribute();

    void setAttribute(Document attribute);

    /**
     * Set a new owner of this collection.
     * @param user New owner for this collection.
     */
    void setUser(User user);

    /**
     * Identify the owner of the collection.
     * @return Returns owner of the collection.
     */
    User getUser(); // FIXME: Is ArtifactCollectionFactory needed?

    /**
     * When created by a factory this method is called to
     * initialize the collection.
     * @param identifier The identifier from collection database
     * @param factory    The factory which created this collection.
     * @param context    The global context of the runtime system.
     * @param data       The data which can be use to setup a collection with
     *                   more details.
     */
    void setup(
        String                    identifier,
        String                    name,
        Date                      creationTime,
        long                      ttl,
        ArtifactCollectionFactory factory,
        Object                    context,
        Document                  data);


    Document describe(CallContext context);

    //TODO: create LifeCycle interface
    /**
     * Called from artifact database when an artifact is
     * going to be removed from system.
     * @param context The global context of the runtime system.
     */
    void endOfLife(Object context);

    /**
     * Internal hash of this collection.
     * @return Returns hash that should stay the same if the internal
     *         value has not changed. Useful for caching
     */
    String hash();


    /**
     * Called from artifact database before an artifact is
     * going to be exported as xml document.
     * @param context The global context of the runtime system.
     */
    void cleanup(Object context);

    void addArtifact(Artifact artifact, Document attributes, CallContext context);

    void removeArtifact(Artifact artifact, CallContext context);

    Artifact [] getArtifacts(CallContext context);

    Document getAttribute(Artifact artifactCall, CallContext context);

    void setAttribute(Artifact artifact, Document document, CallContext context);

    /**
     * Produce output for this collection.
     * @param type Specifies the output type of the action.
     * @param format Specifies the format of the output.
     * @param out Stream to write the result data to.
     * @param context The global context of the runtime system.
     * @throws IOException Thrown if an I/O occurs.
     */
    void out(
        String       type,
        Document     format,
        OutputStream out,
        CallContext  context)
    throws IOException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
