/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

/**
 * The proxy artifact is a wrapper around another artifact. It simply forwards
 * the interface calls to this underlaying artifact.
 * The reason for using proxy artifacts is enable the workflow to exchange
 * artifacts at any time by something else without losing the concrete
 * artifact. From the outside it always looks like there is only one
 * distinct artifact.<br>
 *
 * An inner artifact is able to replace itself by indirectly hand over
 * the replacement via the call context to the proxy artifact.<br>
 * To do so the proxied artifact has to call
 * <code>callContext.getContextValue(EPLACE_PROXY, replacement);</code>.
 * After the current call (describe, feed, advance and out) of the proxied
 * artifact is finished the proxy artifact replaces the former proxied artifact
 * with the replacement.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class ProxyArtifact
extends      DefaultArtifact
{
    /**
     * Key to signal that the proxied artifact should be replaced.
     */
    public static final Object REPLACE_PROXY = new Object();

    private static Logger logger = LogManager.getLogger(ProxyArtifact.class);

    /**
     * The proxied artifact.
     */
    protected Artifact proxied;

    /**
     * Default constructor.
     */
    public ProxyArtifact() {
    }

    /**
     * Constructor to create a new proxy artifact around a given artifact.
     * @param proxied The artifact to be proxied.
     */
    public ProxyArtifact(Artifact proxied) {
        this.proxied = proxied;
    }

    /**
     * The currently proxied artifact.
     * @return The proxied artifact.
     */
    public Artifact getProxied() {
        return proxied;
    }

    /**
     * Explicitly set the proxied artifacts.
     * @param proxied
     */
    public void setProxied(Artifact proxied) {
        this.proxied = proxied;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;

        if (proxied != null)
            proxied.setIdentifier(identifier);
    }

    /**
     * Method to check if the current proxied artifact should be replaced
     * by a new one coming from the call context.
     * @param callContext
     */
    protected void checkReplacement(CallContext callContext) {
        Object replacement = callContext.getContextValue(REPLACE_PROXY);
        if (replacement instanceof Artifact) {
            setProxied((Artifact)replacement);
        }
    }

    @Override
    public String hash() {
        return proxied != null
            ? proxied.hash()
            : super.hash();
    }

    @Override
    public Document describe(Document data, CallContext context) {
        try {
            return proxied != null
                ? proxied.describe(data, context)
                : super.describe(data, context);
        }
        finally {
            checkReplacement(context);
        }
    }

    @Override
    public Document advance(Document target, CallContext context) {
        try {
            return proxied != null
                ? proxied.advance(target, context)
                : super.advance(target, context);
        }
        finally {
            checkReplacement(context);
        }
    }

    @Override
    public Document feed(Document target, CallContext context) {
        try {
            return proxied != null
                ? proxied.feed(target, context)
                : super.feed(target, context);
        }
        finally {
            checkReplacement(context);
        }
    }

    @Override
    public void out(
        Document     format,
        OutputStream out,
        CallContext  context
    )
    throws IOException
    {
        try {
            if (proxied != null) {
                proxied.out(format, out, context);
            }
            else {
                super.out(format, out, context);
            }
        }
        finally {
            checkReplacement(context);
        }
    }

    @Override
    public void endOfLife(Object context) {
        if (proxied != null) {
            proxied.endOfLife(context);
        }
        else {
            super.endOfLife(context);
        }
    }

    @Override
    public void cleanup(Object context) {
        if (proxied != null)
            proxied.cleanup(context);
        else
            super.cleanup(context);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
