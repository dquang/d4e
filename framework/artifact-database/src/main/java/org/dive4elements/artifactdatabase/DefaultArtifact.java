/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

/**
 * Trivial implementation of an artifact. Useful to be subclassed.
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultArtifact
implements   Artifact
{
    private static Logger logger = LogManager.getLogger(DefaultArtifact.class);

    /**
     * The identifier of the artifact.
     */
    protected String identifier;

    /**
     * The name of the artifact.
     */
    protected String name;


    /**
     * Default constructor.
     */
    public DefaultArtifact() {
    }


    public void setIdentifier(String identifier) {
        if (logger.isDebugEnabled()) {
            logger.debug("Change identifier: "
                + this.identifier + " -> " + identifier);
        }
        this.identifier = identifier;
    }

    public String identifier() {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.identifier: " + identifier);
        }
        return this.identifier;
    }


    public String hash() {
        String hash = String.valueOf(hashCode());
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.hashCode: "
                + identifier + " (" + hash + ")");
        }
        return hash;
    }

    public Document describe(Document data, CallContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.describe: " + identifier);
        }
        return XMLUtils.newDocument();
    }

    public Document advance(Document target, CallContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.advance: " + identifier);
        }
        return XMLUtils.newDocument();
    }

    public Document feed(Document target, CallContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.feed: " + identifier);
        }
        return XMLUtils.newDocument();
    }

    public void out(
        Document     format,
        OutputStream out,
        CallContext  context
    )
    throws IOException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.out: " + identifier);
        }
    }

    public void out(
        String       type,
        Document     format,
        OutputStream out,
        CallContext  context
    )
    throws IOException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.out: " + identifier);
        }
    }

    public void setup(String identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     facets)
    {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.setup: " + identifier);
        }
        this.identifier = identifier;
    }

    public void endOfLife(Object context) {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.endOfLife: " + identifier);
        }
    }

    public void cleanup(Object context) {
        if (logger.isDebugEnabled()) {
            logger.debug("DefaultArtifact.cleanup: " + identifier);
        }
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
