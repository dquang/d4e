/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.http.response;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.restlet.Response;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StringResponseHandler extends StreamResponseHandler {

    public StringResponseHandler() {
    }

    @Override
    public Object handle(Response response) throws IOException {
        InputStream  in  = (InputStream) super.handle(response);
        OutputStream out = new ByteArrayOutputStream();

        byte[] b = new byte[4096];
        int i = -1;
        while ((i = in.read(b)) > 0) {
            out.write(b, 0, i);
        }
        out.close();

        return out.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
