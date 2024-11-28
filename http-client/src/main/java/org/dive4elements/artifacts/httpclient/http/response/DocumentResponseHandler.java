/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.http.response;

import java.io.IOException;

import org.restlet.Response;
import org.restlet.representation.Representation;

import org.dive4elements.artifacts.httpclient.utils.XMLUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DocumentResponseHandler implements ResponseHandler {

    public DocumentResponseHandler() {
    }

    @Override
    public Object handle(Response response) throws IOException {
        Representation output = response.getEntity();
        return XMLUtils.readDocument(output.getStream());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
