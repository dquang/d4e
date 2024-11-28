/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

/**
 * The standard exception if something goes wrong inside the artifact database.
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class ArtifactDatabaseException
extends      Exception
{
    /**
     * The default constructor.
     */
    public ArtifactDatabaseException() {
    }

    /**
     * Constructor with a string message.
     * @param msg
     */
    public ArtifactDatabaseException(String msg) {
        super(msg);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
