/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.exceptions;

import java.io.IOException;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ConnectionException extends IOException {

    public ConnectionException(String msg) {
        super(msg);
    }

    public ConnectionException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
