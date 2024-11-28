/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import org.w3c.dom.Document;

/**
 * Interface of the core component of the artifact system: <strong>The artifact</strong>.
 * <br>
 *
 * An artifact is an abstract data type offering the following methods:
 *
 * <ol>
 *   <li>{@link #identifier() identifier()}: Returns a gobally unique identfier
 *        of this artifact.</li>
 *   <li>{@link #hash() hash()}: Returns a hash value over the internal state
 *        of this artifact.</li>
 *   <li>{@link #describe(Document, CallContext)}: Returns a description of this artifact.</li>
 *   <li>{@link #advance(Document, CallContext) advance()}: Advances this artifact
 *       to the next internal state</li>
 *   <li>{@link #feed(Document, CallContext) feed()}: Feed new data into this artifact.</li>
 *   <li>{@link #out(Document, OutputStream, CallContext) out()}: Produces output for this artifact.</li>
 * </ol>
 *
 * There are two more methods involved with the life cycle of the are:
 * <ol>
 *   <li>{@link #setup(String, ArtifactFactory, Object, CallMeta, Document) setup()}:
 *   Called after created by the factory.</li>
 *   <li>{@link #endOfLife(Object) endOfLife()}: Called when the artifact
 *                                               is going to be removed from
 *                                               system. Useful to clean up.</li>
 * </ol>
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface Artifact
extends          Serializable
{
    /**
     * The artifact name.
     * @return The name of the artifact.
     */
    public String getName();

    /**
     * Set the artifact name.
     * @param name The name of the artifact.
     */
    public void setName(String name);

    /**
     * Set a new identifier for this artifact.
     * @param identifier New identifier for this artifact.
     */
    public void setIdentifier(String identifier);

    /**
     * Identify this artifact.
     * @return Returns unique string to identify this artifact globally.
     */
    String identifier();

    /**
     * Internal hash of this artifact.
     * @return Returns hash that should stay the same if the internal
     *         value has not changed. Useful for caching
     */
    String hash();

    /**
     * A description used to build a interface to interact with this artifact.
     * @param data General input data. Useful to produces specific descriptions.
     * @param context The global context of the runtime system.
     * @return An XML representation of the current state of the artifact.
     */
    Document describe(Document data, CallContext context);

    /**
     * Change the internal state of the artifact.
     * @return An XML representation of the success of the advancing.
     * @param target Target of internal state to move to.
     * @param context The global context of the runtime system.
     */
    Document advance(Document target, CallContext context);

    /**
     * Feed data into this artifact.
     * @param data Data to feed artifact with.
     * @param context The global context of the runtime system.
     * @return An XML representation of the success of the feeding.
     */
    Document feed(Document data, CallContext context);

    /**
     * Produce output for this artifact.
     * @param format Specifies the format of the output.
     * @param out Stream to write the result data to.
     * @param context The global context of the runtime system.
     * @throws IOException Thrown if an I/O occurs.
     */
    void out(
        Document     format,
        OutputStream out,
        CallContext  context)
    throws IOException;

    /**
     * Produce output for this artifact.
     * @param type Specifies the type of the output.
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

    /**
     * When created by a factory this method is called to
     * initialize the artifact.
     *
     * @param identifier The identifier from artifact database
     * @param factory    The factory which created this artifact.
     * @param context    The global context of the runtime system.
     * @param data       The data which can be use to setup an artifact with
     *                   more details.
     */
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     facets);

    /**
     * Called from artifact database when an artifact is
     * going to be removed from system.
     * @param context The global context of the runtime system.
     */
    public void endOfLife(Object context);


    /**
     * Called from artifact database before an artifact is
     * going to be exported as xml document.
     * @param context The global context of the runtime system.
     */
    public void cleanup(Object context);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
