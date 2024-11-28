/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.common.utils.Config;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.ArtifactCollectionFactory;

import java.util.Date;


/**
 * The default implementation of a ArtifactCollectionFactory.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultArtifactCollectionFactory
implements   ArtifactCollectionFactory
{
    /** The logger that is used in this factory.*/
    private static Logger logger =
        LogManager.getLogger(DefaultArtifactCollectionFactory.class);

    /** XPath to access the TTL of this artifact.*/
    public static final String XPATH_TTL = "@ttl";

    /** XPath to access the name of this factory.*/
    public static final String XPATH_NAME = "@name";

    /** XPath to access the description of this artifact factory.*/
    public static final String XPATH_DESCRIPTION = "@description";

    /**
     * XPath to access the class name of the artifacts to be build
     * by this factory.
     */
    public static final String XPATH_ARTIFACTCOLLECTION = "@artifact-collection";

    /**
     * Default description of this factory if none is given by the
     * configuration.
     */
    public static final String DEFAULT_DESCRIPTION =
        "No description available";

    /**
     * Class to load if no artifact class is given in the configuration.
     */
    public static final String DEFAULT_ARTIFACTCOLLECTION =
        "org.dive4elements.artifactdatabase.DefaultArtifact";


    /** The name of the factory.*/
    protected String name;

    /** The description of the factory.*/
    protected String description;

    /** The class that is used to instantiate new ArtifactCollection.*/
    protected Class clazz;

    /** The time to live of the artifact collection build by this factory.*/
    protected Long ttl;


    /**
     * The default constructor.
     */
    public DefaultArtifactCollectionFactory() {
    }


   /**
    * The short name of this factory.
    *
    * @return the name of this factory.
    */
    public String getName() {
        return name;
    }


    /**
     * Description of this factory.
     *
     * @return description of the factory.
     */
    public String getDescription() {
        return description;
    }


    /**
     * Returns the time to live of the given artifact.
     */
    public Long timeToLiveUntouched(
        ArtifactCollection collection,
        Object             context)
    {
        return ttl;
    }


    /**
     * Create a new artifact of certain type, given a general purpose context and
     * an identifier.
     * @param context a context from the ArtifactDatabase.
     * @param identifier unique identifer for the new artifact
     * @param data  the data containing more details for the setup of an Artifact.
     * @return a new {@linkplain org.dive4elements.artifacts.ArtifactCollection ArtifactCollection}
     */
    public ArtifactCollection createCollection(
        String   identifier,
        String   name,
        Date     creationTime,
        long     ttl,
        Document data,
        Object   context
    ) {
        try {
            ArtifactCollection collection =
                (ArtifactCollection) clazz.newInstance();

            collection.setup(identifier,
                name,
                creationTime,
                ttl,
                this,
                context,
                data);

            return collection;
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

    /**
     * Setup the factory with a given configuration
     * @param config the configuration
     * @param factoryNode the ConfigurationNode of this Factory
     */
    public void setup(Document config, Node factoryNode) {
        String ttlString = Config.getStringXPath(factoryNode, XPATH_TTL);
        if (ttlString != null) {
            try {
                ttl = Long.valueOf(ttlString);
            }
            catch (NumberFormatException nfe) {
                logger.warn("'" + ttlString + "' is not an integer.");
            }
        }

        description = Config.getStringXPath(
            factoryNode, XPATH_DESCRIPTION, DEFAULT_DESCRIPTION);

        name = Config.getStringXPath(factoryNode, XPATH_NAME, toString());

        String artifactCollection = Config.getStringXPath(
            factoryNode, XPATH_ARTIFACTCOLLECTION, DEFAULT_ARTIFACTCOLLECTION);

        try {
            clazz = Class.forName(artifactCollection);
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }

        if (clazz == null) {
            clazz = DefaultArtifactCollection.class;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
