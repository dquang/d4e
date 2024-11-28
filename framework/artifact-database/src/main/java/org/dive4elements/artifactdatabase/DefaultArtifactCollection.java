/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.ArtifactCollectionFactory;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.User;

import org.dive4elements.artifacts.common.utils.XMLUtils;


/**
 * Trivial implementation of an artifact collection. Useful to be subclassed.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultArtifactCollection
implements   ArtifactCollection
{
    /** The logger used in this class. */
    private static Logger logger =
        LogManager.getLogger(DefaultArtifactCollection.class);

    /** The identifier of the collection. */
    protected String identifier;

    /** The identifier of the collection. */
    protected String name;

    /** The owner of this collection. */
    protected User user;

    /** The attribute of this collection. */
    protected Document attribute;

    /** The artifacts stored in this collection. */
    protected List<Artifact> artifacts;

    /**
     * The attributes used for the artifacts stored in this collection. The key
     * of this map represents the identifier of the artifact which the attribute
     * belong to.
     */
    protected Map<String, Document> attributes;

    /** The creation time of this collection.*/
    protected Date creationTime;

    protected long ttl;


    /**
     * Default constructor.
     */
    public DefaultArtifactCollection() {
    }


    /**
     * When created by a factory this method is called to
     * initialize the collection.
     * @param identifier The identifier from collection database
     * @param factory    The factory which created this collection.
     * @param context    The global context of the runtime system.
     * @param data       The data which can be use to setup a collection with
     *                   more details.
     */
    public void setup(
        String                    identifier,
        String                    name,
        Date                      creationTime,
        long                      ttl,
        ArtifactCollectionFactory factory,
        Object                    context,
        Document                  data)
    {
        logger.debug("DefaultArtifactCollection.setup: " + identifier);

        artifacts  = new ArrayList<Artifact>();
        attributes = new HashMap<String, Document>();

        setIdentifier(identifier);
        setName(name);
        setCreationTime(creationTime);
        setTTL(ttl);
        setAttribute(data);
    }


    public Document describe(CallContext context) {
        logger.debug("DefaultArtifactCollection.describe: " + identifier);

        return XMLUtils.newDocument();
    }


    /**
     * Set a new identifier for this collection.
     * @param identifier New identifier for this collection.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    /**
     * Identify this collection.
     * @return Returns unique string to identify this collection globally.
     */
    public String identifier() {
        return identifier;
    }


    /**
     * Name of this collection.
     * @return Returns the name of this collection
     */
    public String getName() {
        return name;
    }

    /**
     * Name of this collection.
     * @param name the name of this collection
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Set a new owner of this collection.
     * @param user New owner for this collection.
     */
    public void setUser(User user) {
        this.user = user;
    }


    /**
     * Identify the owner of the collection.
     * @return Returns owner of the collection.
     */
    public User getUser() {
        return user;
    }


    /**
     * Returns the creation time of the collection.
     *
     * @return the creation time of the collection.
     */
    public Date getCreationTime() {
        return creationTime;
    }


    /**
     * Sets the creation time of the collection.
     *
     * @param creationTime The new creation time.
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }


    public long getTTL() {
        return ttl;
    }


    public void setTTL(long ttl) {
        this.ttl = ttl;
    }


    /**
     * Returns the attribute of the collection.
     *
     * @return the attribute of the collection.
     */
    public Document getAttribute() {
        return attribute;
    }


    /**
     * Sets the attribute of the collection.
     *
     * @param attribute The attribute of this collection.
     */
    public void setAttribute(Document attribute) {
        this.attribute = attribute;
    }


    /**
     * Called from artifact database when an artifact is
     * going to be removed from system.
     * @param context The global context of the runtime system.
     */
    public void endOfLife(Object context) {
        logger.debug("DefaultArtifactCollection.endOfLife");
    }


    /**
     * Internal hash of this collection.
     * @return Returns hash that should stay the same if the internal
     *         value has not changed. Useful for caching
     */
    public String hash() {
        logger.debug("DefaultArtifactCollection.hash");

        return String.valueOf(hashCode());
    }


    /**
     * Called from artifact database before an artifact is
     * going to be exported as xml document.
     * @param context The global context of the runtime system.
     */
    public void cleanup(Object context) {
        logger.debug("DefaultArtifactCollection.cleanup");
    }


    /**
     * Adds a new artifact to this collection.
     *
     * @param artifact The new artifact.
     * @param attribute The attributes used for this artifact.
     * @param context The CallContext.
     */
    public void addArtifact(
        Artifact    artifact,
        Document    attribute,
        CallContext context)
    {
        logger.debug("DefaultArtifactCollection.addArtifact");

        artifacts.add(artifact);
        attributes.put(artifact.identifier(), attribute);
    }


    /**
     * Removes the given artifact from this collection.
     *
     * @param artifact The artifact that should be removed.
     * @param context The CallContext.
     */
    public void removeArtifact(Artifact artifact, CallContext context) {
        logger.debug("DefaultArtifactCollection.removeArtifact");

        if (artifact == null) {
            return;
        }

        artifacts.remove(artifact);
        attributes.remove(artifact.identifier());
    }


    /**
     * Returns a list of artifacts that are stored in this collection.
     *
     * @param context The CallContext.
     *
     * @return the list of artifacts stored in this collection.
     */
    public Artifact[] getArtifacts(CallContext context) {
        logger.debug("DefaultArtifactCollection.getArtifacts");

        return (Artifact[]) artifacts.toArray();
    }


    /**
     * Returns the attribute document for the given artifact.
     *
     * @param artifact The artifact.
     * @param context The CallContext.
     *
     * @return a document that contains the attributes of the artifact.
     */
    public Document getAttribute(Artifact artifact, CallContext context) {
        logger.debug("DefaultArtifactCollection.getAttribute");

        return attributes.get(artifact.identifier());
    }


    /**
     * Set the attribute for the given artifact.
     *
     * @param artifact The artifact of the attribute.
     * @param document The new attribute of the artifact.
     * @param context The CallContext.
     */
    public void setAttribute(
        Artifact    artifact,
        Document    document,
        CallContext context)
    {
        logger.debug("DefaultArtifactCollection.setAttribute");

        attributes.put(artifact.identifier(), document);
    }


    /**
     * Produce output for this collection.
     * @param type Specifies the output type.
     * @param format Specifies the format of the output.
     * @param out Stream to write the result data to.
     * @param context The global context of the runtime system.
     * @throws IOException Thrown if an I/O occurs.
     */
    public void out(
        String       type,
        Document     format,
        OutputStream out,
        CallContext  context)
    throws IOException
    {
        logger.debug("DefaultArtifactCollection.out");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
