package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifacts.ArtifactDatabase;

import org.restlet.Component;
import org.restlet.Server;

import org.restlet.ext.jetty.HttpServerHelper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class JettyServer
extends      Standalone
{
    private static Logger logger = LogManager.getLogger(JettyServer.class);

    @Override
    public void startAsServer(ArtifactDatabase db) {

        Component component = new Component();

        RestApp app = new RestApp(db);

        Server server = createServer();

        // TODO: Do more sophisticated Jetty server configuration here.

        component.getServers().add(server);

        component.getDefaultHost().attach(app);

        logServerStart();

        HttpServerHelper serverHelper = new HttpServerHelper(server);

        try {
            serverHelper.start();
        }
        catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
