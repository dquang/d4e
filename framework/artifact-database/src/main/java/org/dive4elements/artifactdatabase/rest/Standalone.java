/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.ArtifactDatabase;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.Component;
import org.restlet.Server;

import org.restlet.data.Protocol;

import org.w3c.dom.Document;

/**
 * Starts an HTTP server bound to a RestApp.
 * The server (binding interface and port) is configure via the
 * global configuration.
 *
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class Standalone
implements   HTTPServer
{
    private static Logger logger = LogManager.getLogger(Standalone.class);

    /**
     * XPath to figure out the port where to listen from the
     * global configuration.
     */
    public static final String REST_PORT =
        "/artifact-database/rest-server/port/text()";

    /**
     * XPath to figure out from global configuration
     * which network interface to use to bind the HTTP server.
     */
    public static final String LISTEN_INTERFACE =
        "/artifact-database/rest-server/listen/text()";

    /**
     * The default port of the HTTP server: 8181
     */
    public static final int DEFAULT_PORT = 8181;

    public static final String MAX_THREADS =
        "/artifact-database/rest-server/max-threads/text()";

    public static final String MAX_THREADS_DEFAULT =
        "1024";

    protected int     port;

    protected String  listen;

    protected String  maxThreads;

    public Standalone() {
    }

    @Override
    public void setup(Document document) {
        String portString = XMLUtils.xpathString(document, REST_PORT, null);

        port = DEFAULT_PORT;

        if (portString != null) {
            try {
                port = Integer.parseInt(portString);
                if (port < 0) {
                    throw new NumberFormatException();
                }
            }
            catch (NumberFormatException nfe) {
                logger.error("rest port is not a positive integer value.", nfe);
                return;
            }
        }

        listen     = XMLUtils.xpathString(document, LISTEN_INTERFACE, null);
        maxThreads = XMLUtils.xpathString(document, MAX_THREADS, null);
    }

    protected Server createServer() {
        return listen != null && listen.length() > 0
            ? new Server(Protocol.HTTP, listen, port)
            : new Server(Protocol.HTTP, port);
    }

    protected void logServerStart() {
        logger.info("Starting " + getClass().getName() + " HTTP server on "
            + (listen != null ? listen : "*")
            + ":" + port);
    }

    /**
     * Builds a RestApp wrapped around the given artifact database,
     * and bind this application to HTTP server. The HTTP server
     * is configured by the global configuration. If no port is
     * given by the configuration the default port is used. If
     * no interface is given the HTTP server is reachable from
     * all interfaces.
     * @param db The artifact database to be exposed via the
     * REST application.
     */
    @Override
    public void startAsServer(ArtifactDatabase db) {

        RestApp app = new RestApp(db);

        Component component = new Component();

        Server server = createServer();

        component.getServers().add(server);

        server.getContext().getParameters().add(
            "maxThreads", maxThreads != null && maxThreads.length() > 0
                ? maxThreads
                : MAX_THREADS_DEFAULT);

        component.getDefaultHost().attach(app);

        logServerStart();

        try {
            component.start();
        }
        catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
