package org.dive4elements.artifactdatabase.rest;

import org.restlet.representation.Representation;

import java.io.Reader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;

import java.nio.ByteBuffer;

import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;

public class ByteArrayRepresentation
extends      Representation
{
    protected byte [] data;

    public ByteArrayRepresentation(MediaType mediaType, byte [] data) {
        super(mediaType);
        this.data = data;
    }

    @Override
    public long getSize() {
        return data.length;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        return null;
    }

    @Override
    public Reader getReader() throws IOException {
        return new InputStreamReader(getStream());
    }

    @Override
    public InputStream getStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public void write(Writer writer) throws IOException {
        writer.append(ByteBuffer.wrap(data).asCharBuffer());
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        writableChannel.write(ByteBuffer.wrap(data));
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(data);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
