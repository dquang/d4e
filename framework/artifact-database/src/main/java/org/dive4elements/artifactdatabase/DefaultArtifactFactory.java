/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.ArtifactSerializer;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Trivial implementation of the ArtifactFactory interface.
 * Time to live (ttl), name and description are configured
 * via the Node given to #setup(Document, Node) with attributes
 * of same name. The class name of the artifacts to be build by this
 * factory is configures with the attribute 'artifact'.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultArtifactFactory
implements   ArtifactFactory
{
    private static Logger logger =
        LogManager.getLogger(DefaultArtifactFactory.class);

    /**
     * XPath to access the TTL of this artifact.
     */
    public static final String XPATH_TTL         = "@ttl";
    /**
     * XPath to access the name of this factory.
     */
    public static final String XPATH_NAME        = "@name";
    /**
     * XPath to access the description of this artifact factory.
     */
    public static final String XPATH_DESCRIPTION = "@description";
    /**
     * XPath to access the class name of the artifacts to be build
     * by this factory.
     */
    public static final String XPATH_ARTIFACT    = "@artifact";
    /**
     * XPath to access the static facets configured for artifacts
     */
    public static final String XPATH_ARTIFACT_CONFIG =
        "/artifact-database/artifacts/artifact[@name=$name]/load-facets/facet";

    /**
     * XPath to access the static facets configured for artifacts
     */
    public static final String XPATH_ARTIFACT_NAME =
        "/artifact-database/artifacts/artifact/@name";

    /**
     * Default description of this factory if none is given by the
     * configuration.
     */
    public static final String DEFAULT_DESCRIPTION =
        "No description available";

    /**
     * Class to load if no artifact class is given in the configuration.
     */
    public static final String DEFAULT_ARTIFACT =
        "org.dive4elements.artifactdatabase.DefaultArtifact";

    /**
     * The Time to live of the artifacts build by this factory.
     */
    protected Long   ttl;

    /**
     * The name of this factory.
     */
    protected String name;

    /**
     * The description of this factory.
     */
    protected String description;

    /**
     * The class of the artifacts to be build by this factory.
     */
    protected Class  artifactClass;

    /**
     * The name of the artifacts to be build by this factory.
     */
    protected String  artifactName;

    /**
     * The list of facets the generated artifact creates on instantiation.
     */
    protected List<Class> facetClasses;

    /**
     * Default constructor.
     */
    public DefaultArtifactFactory() {
        facetClasses = new ArrayList<Class>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Artifact createArtifact(
        String        identifier,
        GlobalContext context,
        CallMeta      callMeta,
        Document      data
    ) {
        try {
            Artifact artifact =
                (Artifact)artifactClass.newInstance();
            String oldName = artifact.getName();

            if (oldName == null || oldName.length() == 0) {
                artifact.setName(name);
            }
            artifact.setup(
                identifier,
                this,
                context,
                callMeta,
                data,
                facetClasses);

            return artifact;
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

    public void setup(Document document, Node factoryNode) {
        boolean debug = logger.isDebugEnabled();

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

        name = Config.getStringXPath(
            factoryNode, XPATH_NAME, toString());

        if (debug) {
            logger.debug("setting up " + name);
        }

        String artifact = Config.getStringXPath(
            factoryNode, XPATH_ARTIFACT, DEFAULT_ARTIFACT);

        artifactName = Config.getStringXPath(
            document, XPATH_ARTIFACT_NAME, "default");

        if (debug) {
            logger.debug("artifact name: " + artifactName);
        }
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("name", name);
        NodeList facets = (NodeList) XMLUtils.xpath(
            document,
            XPATH_ARTIFACT_CONFIG,
            XPathConstants.NODESET,
            null,
            variables);

        for (int i = 0, F = facets.getLength(); i < F; i++) {
            Element element = (Element)facets.item(i);
            String className = element.getAttribute("class");

            if (debug) {
                logger.debug("load facet class: " + className);
            }

            try {
                facetClasses.add(Class.forName(className));
            }
            catch (ClassNotFoundException cnfe) {
                logger.error(cnfe.getLocalizedMessage(), cnfe);
            }
        }

        try {
            artifactClass = Class.forName(artifact);
        }
        catch (ClassNotFoundException cnfe) {
            logger.error(cnfe.getLocalizedMessage(), cnfe);
        }

        if (artifactClass == null) {
            artifactClass = DefaultArtifact.class;
        }
    }

    public Long timeToLiveUntouched(Artifact artifact, Object context) {
        return ttl;
    }

    public ArtifactSerializer getSerializer() {
        return DefaultArtifactSerializer.INSTANCE;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
