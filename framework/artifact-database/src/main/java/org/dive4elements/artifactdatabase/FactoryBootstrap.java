/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.artifacts.ArtifactCollectionFactory;
import org.dive4elements.artifacts.ArtifactContextFactory;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.Hook;
import org.dive4elements.artifacts.ServiceFactory;
import org.dive4elements.artifacts.UserFactory;

import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.artifactdatabase.rest.HTTPServer;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Bootstrap facility for the global context and the artifact factories.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class FactoryBootstrap
{
    private static Logger logger = LogManager.getLogger(FactoryBootstrap.class);

    /**
     * XPath to figure out the class name of the context factory from
     * the global configuration.
     */
    public static final String CONTEXT_FACTORY =
        "/artifact-database/factories/context-factory/text()";

    /**
     * The name of the default context factory.
     */
    public static final String DEFAULT_CONTEXT_FACTORY =
        "org.dive4elements.artifactdatabase.DefaultArtifactContextFactory";

    /**
     * XPath to figure out the names of the artifact factories from
     * the global configuration to be exposed by the artifact database.
     */
    public static final String ARTIFACT_FACTORIES =
        "/artifact-database/factories/artifact-factories/artifact-factory";

    /**
     * XPath to figure out the names of the service factories from
     * the global configuration to build the services offered by the
     * artifact database.
     */
    public static final String SERVICE_FACTORIES =
        "/artifact-database/factories/service-factories/service-factory";

    /**
     * XPath to figure out the class name of the user factory from global
     * configuration.
     */
    public static final String USER_FACTORY =
        "/artifact-database/factories/user-factory";

    /**
     * The name of the default user factory.
     */
    public static final String DEFAULT_USER_FACTORY =
        "org.dive4elements.artifactdatabase.DefaultUserFactory";

    /**
     * XPath to figure out the class name of the collection factory from global
     * configuration.
     */
    public static final String COLLECTION_FACTORY =
        "/artifact-database/factories/collection-factory";

    /**
     * The name of the default user factory.
     */
    public static final String DEFAULT_COLLECTION_FACTORY =
        "org.dive4elements.artifactdatabase.DefaultArtifactCollectionFactory";

    /**
     * XPath to figure out the secret used to sign the artifact exports
     * made by the artfifact database server.
     */
    public static final String EXPORT_SECRET =
        "/artifact-database/export-secret/text()";

    /**
     * XPAth that points to a configuration node for a CallContext.Listener.
     */
    public static final String CALLCONTEXT_LISTENER =
        "/artifact-database/callcontext-listener";

    /**
     * XPath that points to configuration nodes for hooks.
     */
    public static final String HOOKS =
        "/artifact-database/hooks/hook";

    public static final String HTTP_SERVER =
        "/artifact-database/rest-server/http-server/text()";

    public static final String DEFAULT_HTTP_SERVER =
        "org.dive4elements.artifactdatabase.rest.Standalone";

    public static final String LIFETIME_LISTENERS =
        "/artifact-database/lifetime-listeners/listener";

    public static final String BACKEND_LISTENERS =
        "/artifact-database/backend-listeners/listener";

    /**
     * Default export signing secret.
     * <strong>PLEASE CHANGE THE SECRET VIA THE XPATH EXPORT_SECRET
     * IN THE CONFIGURATION.</strong>.
     */
    public static final String DEFAULT_EXPORT_SECRET =
        "!!!CHANGE ME! I'M NO SECRET!!!";

    /**
     * Reference to the global context build by the global context factory.
     */
    protected GlobalContext context;

    /**
     * List of the artifact factories to be exposed by the
     * artifact database.
     */
    protected ArtifactFactory [] artifactFactories;

    /**
     * List of service factories which creates services that are
     * exposed by the artifact database.
     */
    protected ServiceFactory [] serviceFactories;

    /**
     * The factory that is used to create and list users.
     */
    protected UserFactory userFactory;

    /**
     * The factory that is used to create new artifact collections.
     */
    protected ArtifactCollectionFactory collectionFactory;

    /**
     * The CallContext.Listener.
     */
    protected CallContext.Listener callContextListener;

    protected List<Hook> postFeedHooks;

    protected List<Hook> postAdvanceHooks;

    protected List<Hook> postDescribeHooks;

    protected List<LifetimeListener> lifetimeListeners;

    protected List<BackendListener> backendListeners;

    /**
     * byte array holding the export signing secret.
     */
    protected byte [] exportSecret;

    protected HTTPServer httpServer;


    /**
     * Default constructor
     */
    public FactoryBootstrap() {
    }

    void buildContext() {
        String className = Config.getStringXPath(
            CONTEXT_FACTORY, DEFAULT_CONTEXT_FACTORY);

        ArtifactContextFactory factory = null;

        try {
            Class clazz = Class.forName(className);
            factory = (ArtifactContextFactory)clazz.newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }
        catch (InstantiationException ie) {
            logger.error(ie.getLocalizedMessage(), ie);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }
        catch (IllegalAccessException iae) {
            logger.error(iae.getLocalizedMessage(), iae);
        }

        if (factory == null) {
            factory = new DefaultArtifactContextFactory();
        }

        logger.info("Using class '" + factory.getClass().getName()
            + "' for context creation.");

        context = factory.createArtifactContext(Config.getConfig());
    }


    /**
     * Scans the global configuration to load the configured collection factory
     * and sets it up.
     */
    protected void loadCollectionFactory() {

        logger.info("loading collection factory.");

        Node factory = Config.getNodeXPath(COLLECTION_FACTORY);

        String className = Config.getStringXPath(
            factory, "text()", DEFAULT_COLLECTION_FACTORY);

        try {
            Class clazz       = Class.forName(className);
            collectionFactory = (ArtifactCollectionFactory) clazz.newInstance();

            collectionFactory.setup(Config.getConfig(), factory);
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }
        catch (InstantiationException ie) {
            logger.error(ie.getLocalizedMessage(), ie);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }
        catch (IllegalAccessException iae) {
            logger.error(iae.getLocalizedMessage(), iae);
        }
    }

    /**
     * Scans the global configuration to load the configured
     * artifact factories and sets them up.
     */
    protected void loadArtifactFactories() {

        logger.info("loading artifact factories");

        ArrayList loadedFactories = new ArrayList();

        NodeList nodes = Config.getNodeSetXPath(ARTIFACT_FACTORIES);

        if (nodes == null) {
            logger.warn("No factories found");
        }

        Document config = Config.getConfig();

        for (int i = 0, N = nodes != null ? nodes.getLength() : 0; i < N; ++i) {
            String className = nodes.item(i).getTextContent().trim();

            ArtifactFactory factory = null;

            try {
                Class clazz = Class.forName(className);
                factory = (ArtifactFactory)clazz.newInstance();
            }
            catch (ClassNotFoundException cnfe) {
                logger.error(cnfe.getLocalizedMessage(), cnfe);
            }
            catch (InstantiationException ie) {
                logger.error(ie.getLocalizedMessage(), ie);
            }
            catch (ClassCastException cce) {
                logger.error(cce.getLocalizedMessage(), cce);
            }
            catch (IllegalAccessException iae) {
                logger.error(iae.getLocalizedMessage(), iae);
            }

            if (factory != null) {
                factory.setup(config, nodes.item(i));
                loadedFactories.add(factory);
                logger.info("Registering '"
                    + factory.getName() + "' as artifact factory.");
            }
        }

        artifactFactories = (ArtifactFactory [])loadedFactories.toArray(
            new ArtifactFactory[loadedFactories.size()]);
    }

    /**
     * Scans the global configuration for the configured service factories
     * and sets them up.
     */
    protected void loadServiceFactories() {

        logger.info("loading service factories");

        ArrayList loadedFactories = new ArrayList();

        NodeList nodes = Config.getNodeSetXPath(SERVICE_FACTORIES);

        if (nodes == null) {
            logger.warn("No factories found");
        }

        Document config = Config.getConfig();

        for (int i = 0, N = nodes != null ? nodes.getLength() : 0; i < N; ++i) {
            String className = nodes.item(i).getTextContent().trim();

            ServiceFactory factory = null;

            try {
                Class clazz = Class.forName(className);
                factory = (ServiceFactory)clazz.newInstance();
            }
            catch (ClassNotFoundException cnfe) {
                logger.error(cnfe.getLocalizedMessage(), cnfe);
            }
            catch (InstantiationException ie) {
                logger.error(ie.getLocalizedMessage(), ie);
            }
            catch (ClassCastException cce) {
                logger.error(cce.getLocalizedMessage(), cce);
            }
            catch (IllegalAccessException iae) {
                logger.error(iae.getLocalizedMessage(), iae);
            }

            if (factory != null) {
                factory.setup(config, nodes.item(i));
                loadedFactories.add(factory);
                logger.info( "Registering '" + factory.getName()
                    + "' as service factory.");
            }
        }

        serviceFactories = (ServiceFactory [])loadedFactories.toArray(
            new ServiceFactory[loadedFactories.size()]);
    }


    /**
     * Scans the global configuration for the configured user factory.
     */
    protected void loadUserFactory() {
        logger.info("loading user factory");

        Node factory = Config.getNodeXPath(USER_FACTORY);

        String className = Config.getStringXPath(
            factory, "text()", DEFAULT_USER_FACTORY);

        try {
            Class clazz = Class.forName(className);
            userFactory = (UserFactory) clazz.newInstance();

            userFactory.setup(Config.getConfig(), factory);
        }
        catch (ClassNotFoundException cnfe) {
                logger.error(cnfe.getLocalizedMessage(), cnfe);
        }
        catch (InstantiationException ie) {
            logger.error(ie.getLocalizedMessage(), ie);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }
        catch (IllegalAccessException iae) {
            logger.error(iae.getLocalizedMessage(), iae);
        }
    }


    protected void loadCallContextListener() {
        logger.info("loading CallContext.Listener");

        Node listener = Config.getNodeXPath(CALLCONTEXT_LISTENER);

        if (listener == null) {
            return;
        }

        String className = Config.getStringXPath(listener, "text()");

        try {
            Class clazz         = Class.forName(className);
            callContextListener = (CallContext.Listener) clazz.newInstance();

            callContextListener.setup(Config.getConfig(), listener);
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }
        catch (InstantiationException ie) {
            logger.error(ie.getLocalizedMessage(), ie);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }
        catch (IllegalAccessException iae) {
            logger.error(iae.getLocalizedMessage(), iae);
        }
    }

    protected void loadHTTPServer() {
        logger.info("loading HTTPServer");

        String className = Config.getStringXPath(
            HTTP_SERVER, DEFAULT_HTTP_SERVER);

        logger.info("using HTTP server: " + className);

        try {
            Class clazz = Class.forName(className);
            httpServer  = (HTTPServer)clazz.newInstance();

            httpServer.setup(Config.getConfig());
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }
        catch (InstantiationException ie) {
            logger.error(ie.getLocalizedMessage(), ie);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }
        catch (IllegalAccessException iae) {
            logger.error(iae.getLocalizedMessage(), iae);
        }
    }

    protected void loadLifetimeListeners() {
        logger.info("loading lifetime listeners");

        NodeList nodes = Config.getNodeSetXPath(LIFETIME_LISTENERS);

        if (nodes == null) {
            logger.debug("no lifetime listeners configure");
            return;
        }

        List<LifetimeListener> ltls = new ArrayList<LifetimeListener>();

        for (int i = 0, N = nodes.getLength(); i < N; ++i) {
            Node node = nodes.item(i);
            String className = node.getTextContent();
            if (className == null
            || (className = className.trim()).length() == 0) {
                continue;
            }
            try {
                Class clazz = Class.forName(className);
                LifetimeListener listener =
                    (LifetimeListener)clazz.newInstance();

                listener.setup(Config.getConfig());

                ltls.add(listener);
            }
            catch (ClassNotFoundException cnfe) {
                logger.error(cnfe.getLocalizedMessage(), cnfe);
            }
            catch (InstantiationException ie) {
                logger.error(ie.getLocalizedMessage(), ie);
            }
            catch (ClassCastException cce) {
                logger.error(cce.getLocalizedMessage(), cce);
            }
            catch (IllegalAccessException iae) {
                logger.error(iae.getLocalizedMessage(), iae);
            }
        }

        lifetimeListeners = ltls;
    }

    protected void loadBackendListeners() {
        logger.info("loading backend listeners");

        NodeList nodes = Config.getNodeSetXPath(BACKEND_LISTENERS);

        if (nodes == null) {
            logger.debug("no backend listeners configure");
            return;
        }

        List<BackendListener> bls = new ArrayList<BackendListener>();

        for (int i = 0, N = nodes.getLength(); i < N; ++i) {
            Node node = nodes.item(i);
            String className = node.getTextContent();
            if (className == null
            || (className = className.trim()).length() == 0) {
                continue;
            }
            try {
                Class clazz = Class.forName(className);
                BackendListener listener =
                    (BackendListener)clazz.newInstance();

                bls.add(listener);
            }
            catch (ClassNotFoundException cnfe) {
                logger.error(cnfe.getLocalizedMessage(), cnfe);
            }
            catch (InstantiationException ie) {
                logger.error(ie.getLocalizedMessage(), ie);
            }
            catch (ClassCastException cce) {
                logger.error(cce.getLocalizedMessage(), cce);
            }
            catch (IllegalAccessException iae) {
                logger.error(iae.getLocalizedMessage(), iae);
            }
        }

        backendListeners = bls;
    }

    protected void loadHooks() {
        logger.info("loading hooks");

        postFeedHooks     = new ArrayList<Hook>();
        postAdvanceHooks  = new ArrayList<Hook>();
        postDescribeHooks = new ArrayList<Hook>();

        NodeList nodes = Config.getNodeSetXPath(HOOKS);

        if (nodes == null) {
            logger.info("No hooks found");
            return;
        }

        for (int i = 0, len = nodes.getLength(); i < len; i++) {
            Node   cfg     = nodes.item(i);
            String applies = Config.getStringXPath(cfg, "@applies");

            if (applies == null || applies.length() == 0) {
                continue;
            }

            Hook     hook  = loadHook(cfg);
            String[] apply = applies.split(",");

            for (String a: apply) {
                a = a.trim().toLowerCase();

                if (a.equals("post-feed")) {
                    postFeedHooks.add(hook);
                }
                else if (a.equals("post-advance")) {
                    postAdvanceHooks.add(hook);
                }
                else if (a.equals("post-describe")) {
                    postDescribeHooks.add(hook);
                }
            }
        }
    }

    protected Hook loadHook(Node hookCfg) {
        if (hookCfg == null) {
            return null;
        }

        Hook hook = null;

        String className = Config.getStringXPath(hookCfg, "@class");

        try {
            Class clazz = Class.forName(className);
            hook        = (Hook) clazz.newInstance();

            hook.setup(hookCfg);
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }
        catch (InstantiationException ie) {
            logger.error(ie.getLocalizedMessage(), ie);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }
        catch (IllegalAccessException iae) {
            logger.error(iae.getLocalizedMessage(), iae);
        }

        return hook;
    }

    /**
     * Fetches the export signing secret from the global configuration.
     * If none is found if defaults to the DEFAULT_EXORT_SECRET which
     * is insecure.
     */
    protected void setupExportSecret() {
        String secret = Config.getStringXPath(EXPORT_SECRET);

        if (secret == null) {
            logger.warn("NO EXPORT SECRET SET! USING INSECURE DEFAULT!");
            secret = DEFAULT_EXPORT_SECRET;
        }

        exportSecret = StringUtils.getUTF8Bytes(secret);
    }

    /**
     * Loads all the dynamic classes configured by the global configuration.
     */
    public void boot() {
        setupExportSecret();
        buildContext();
        loadCollectionFactory();
        loadArtifactFactories();
        loadServiceFactories();
        loadUserFactory();
        loadCallContextListener();
        loadHTTPServer();
        loadHooks();
        loadLifetimeListeners();
        loadBackendListeners();
    }

    /**
     * Returns the artifact collection factory.
     *
     * @return the artifact collection factory.
     */
    public ArtifactCollectionFactory getArtifactCollectionFactory() {
        return collectionFactory;
    }

    /**
     * Returns the list of ready to use artifact factories.
     * @return The list of artifact factories.
     */
    public ArtifactFactory [] getArtifactFactories() {
        return artifactFactories;
    }

    /**
     * Returns the ready to use service factories.
     * @return The list of service factories.
     */
    public ServiceFactory [] getServiceFactories() {
        return serviceFactories;
    }

    /**
     * Returns the user factory.
     *
     * @return the user factory.
     */
    public UserFactory getUserFactory() {
        return userFactory;
    }

    /**
     * Returns the global context created by the global context factory.
     * @return The global context.
     */
    public GlobalContext getContext() {
        return context;
    }

    /**
     * Returns the signing secret to be used when ex- and importing
     * artifacts from and into the artifact database.
     * @return the byte array containg the signing secret.
     */
    public byte [] getExportSecret() {
        return exportSecret;
    }

    /**
     * Returns a CallContext.Listener if configured or null.
     *
     * @return a CallContext.Listener.
     */
    public CallContext.Listener getCallContextListener() {
        return callContextListener;
    }

    public List<Hook> getPostFeedHooks() {
        return postFeedHooks;
    }

    public List<Hook> getPostAdvanceHooks() {
        return postAdvanceHooks;
    }

    public List<Hook> getPostDescribeHooks() {
        return postDescribeHooks;
    }

    public HTTPServer getHTTPServer() {
        return httpServer;
    }

    public List<LifetimeListener> getLifetimeListeners() {
        return lifetimeListeners;
    }

    public List<BackendListener> getBackendListeners() {
        return backendListeners;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
