/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Default implementation of the ArtifactSerializer interface.
 * It uses serialized Java objects which are gzipped and
 * turned into bytes.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultArtifactSerializer
implements   ArtifactSerializer
{
    private static Logger logger =
        LogManager.getLogger(DefaultArtifactSerializer.class);

    /**
     * Static instance to avoid repeated creation of Serializers.
     */
    public static final ArtifactSerializer INSTANCE =
        new DefaultArtifactSerializer();

    /**
     * Default constructor.
     */
    public DefaultArtifactSerializer() {
    }

    public Artifact fromBytes(byte [] bytes) {

        if (bytes == null) {
            return null;
        }

        ObjectInputStream ois = null;

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            GZIPInputStream      gis = new GZIPInputStream(bis);
                                 ois = getObjectInputStream(gis);

            return (Artifact)ois.readObject();
        }
        catch (IOException ioe) {
            logger.error(ioe.getLocalizedMessage(), ioe);
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }
        finally {
            if (ois != null) {
                try { ois.close(); }
                catch (IOException ioe) { }
            }
        }

        return null;
    }

    public byte [] toBytes(Artifact artifact) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream      gos = new GZIPOutputStream(bos);
            ObjectOutputStream    oos = getObjectOutputStream(gos);

            oos.writeObject(artifact);
            oos.flush();
            oos.close();

            return bos.toByteArray();
        }
        catch (IOException ioe) {
            logger.error(ioe.getLocalizedMessage(), ioe);
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Wraps an input stream into an object input stream. You may
     * overwrite this to get a more specialized deserializer.
     * @param is The raw input stream
     * @return An instance of a subclass of ObjectInputStream.
     * @throws IOException Thrown if something went wrong during
     * creation of the object input stream.
     */
    protected ObjectInputStream getObjectInputStream(InputStream is)
    throws    IOException
    {
        return new ObjectInputStream(is);
    }

    /**
     * Wraps an output stream into an object output stream. You may
     * overwrite this to get a more specialized serializer.
     * @param os the raw output stream.
     * @return An instance of a subclass of ObjectOutputStream.
     * @throws IOException Thrown if something went wrong during
     * creation of the object output stream.
     */
    protected ObjectOutputStream getObjectOutputStream(OutputStream os)
    throws    IOException
    {
        return new ObjectOutputStream(os);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
