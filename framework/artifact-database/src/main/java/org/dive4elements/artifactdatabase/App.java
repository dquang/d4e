/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifactdatabase.rest.HTTPServer;


/**
 * Starting point of the artifact database.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class App
{
    /**
     * Starts the artifact database.
     * @param args The commandline arguments. Unused.
     */
    public static void main(String[] args) {

        FactoryBootstrap bootstrap = new FactoryBootstrap();

        bootstrap.boot();

        Backend backend = Backend.getInstance();

        ArtifactDatabaseImpl db = new ArtifactDatabaseImpl(
            bootstrap, backend);

        DatabaseCleaner cleaner = new DatabaseCleaner(
            bootstrap.getContext(),
            backend,
            backend.getSQLExecutor(),
            backend.getConfig());

        HTTPServer httpServer = bootstrap.getHTTPServer();

        bootstrap = null;

        backend.setCleaner(cleaner);

        cleaner.setLockedIdsProvider(db);

        cleaner.start();

        db.start();

        httpServer.startAsServer(db);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
