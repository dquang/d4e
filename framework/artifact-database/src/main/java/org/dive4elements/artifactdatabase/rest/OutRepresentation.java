/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifacts.ArtifactDatabase.DeferredOutput;

import java.io.IOException;
import java.io.OutputStream;

import org.restlet.data.MediaType;

import org.restlet.representation.OutputRepresentation;

/**
 * Special representation to serve the out()-outputs
 * via DeferredOutput efficently .
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class OutRepresentation
extends      OutputRepresentation
{
    /**
     * The deferred output fetched from ArtifactDatabase.out().
     */
    protected DeferredOutput out;

    /**
     * Constructor to create representation with a given MIME type and
     * a deferred output.
     * @param mediaType The MIME type of this representation.
     * @param out The deferred output from the ArtifactDatabase.out() call.
     */
    public OutRepresentation(MediaType mediaType, DeferredOutput out) {
        super(mediaType);
        this.out = out;
    }

    /**
     * Overwrites the write(OutputStream) of OutRepresentation to serve
     * the data from the deferred output.
     * @param output the stream where to write the data into.
     * @throws IOException Thrown if an exception occurred while writing
     * the data to the output stream.
     */
    public void write(OutputStream output) throws IOException {
        out.write(output);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
