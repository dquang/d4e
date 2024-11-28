/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.artifacts.Service;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.ServiceFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Trivial implementation of the ServiceFactory interface.
 * Name and an description are configured by the given Node given to
 * #setup(Document, Node) via the 'name' and 'description' attributes.
 * The name of the class that provides the concrete serice is configured
 * by the 'service' attribute.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultServiceFactory
implements   ServiceFactory
{
    private static Logger logger =
        LogManager.getLogger(DefaultServiceFactory.class);

    /**
     * XPath to access the name of the service.
     */
    public static final String XPATH_NAME        = "@name";
    /**
     * XPath to access the description of the service.
     */
    public static final String XPATH_DESCRIPTION = "@description";
    /**
     * XPath to access the class name of the service to be build by
     * this factory.
     */
    public static final String XPATH_SERVICE     = "@service";

    /**
     * Default description if no description is given in configuration.
     */
    public static final String DEFAULT_DESCRIPTION =
        "No description available";

    /**
     * Loaded service class if no class name is given in the configuration.
     */
    public static final String DEFAULT_SERVICE =
        "org.dive4elements.artifactdatabase.DefaultService";

    /**
     * The name of the service factory.
     */
    protected String name;

    /**
     * The description of the service factory.
     */
    protected String description;

    /**
     * The loaded class used to build the concrete service.
     */
    protected Class  serviceClass;

    /**
     * Default constructor.
     */
    public DefaultServiceFactory() {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Service createService(GlobalContext globalContext) {
        try {
            Service service = (Service)serviceClass.newInstance();

            service.setup(this, globalContext);

            return service;
        }
        catch (InstantiationException ie) {
            logger.error(ie.getLocalizedMessage(), ie);
        }
        catch (IllegalAccessException iae) {
            logger.error(iae.getLocalizedMessage(), iae);
        }
        catch (ClassCastException cce) {
            logger.error(cce.getLocalizedMessage(), cce);
        }

        return null;
    }

    @Override
    public void setup(Document config, Node factoryNode) {

        description = Config.getStringXPath(
            factoryNode, XPATH_DESCRIPTION, DEFAULT_DESCRIPTION);

        name = Config.getStringXPath(
            factoryNode, XPATH_NAME, toString());

        String service = Config.getStringXPath(
            factoryNode, XPATH_SERVICE, DEFAULT_SERVICE);

        try {
            serviceClass = Class.forName(service);
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }

        if (serviceClass == null) {
            serviceClass = DefaultService.class;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
