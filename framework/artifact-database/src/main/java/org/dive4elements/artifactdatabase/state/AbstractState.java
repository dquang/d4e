/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.data.StateData;


/**
 * An abstract implementation of a {@link State}. It implements some basic
 * methods that return the id, description and data. The methods
 * <code>describe()</code> and <code>setup()</code> depend on the concrete class
 * and need to be implemented by those.
 */
public abstract class AbstractState implements State {

    /** The XPath to the ID of the state relative to the state node in the
     * configuration. */
    public static final String XPATH_ID = "@id";

    /** The XPath to the description of the state relative to the state node in
     * the configuration. */
    public static final String XPATH_DESCRIPTION = "@description";

    /** The XPath that points to the help text.*/
    public static final String XPATH_HELP_TEXT = "@helpText";

    /** The XPath to the output nodes of the state configuration. */
    public static final String XPATH_OUTPUT_MODES = "outputmodes/outputmode";

    /** The XPath to the list of facets relative to the output mode it belongs
     * to. */
    public static final String XPATH_FACETS = "facets/facet";

    public static final String XPATH_HELP_URL = "/artifact-database/help-url/text()";

    public static final String HELP_URL = "${help.url}";


    /** The logger that is used in this class. */
    private static Logger logger = LogManager.getLogger(AbstractState.class);


    /** The ID of the state. */
    protected String id;

    /** The description of the state. */
    protected String description;

    /** The help text for this state.*/
    protected String helpText;

    /** The data provided by this state. */
    protected Map<String, StateData> data;

    /** A list of output modes which are available for this state. */
    protected List<Output> outputs;

    private static String helpUrl;


    public AbstractState() {
        outputs = new ArrayList<Output>();
    }

    public static synchronized final String getHelpUrl() {
        if (helpUrl == null) {
            helpUrl = Config.getStringXPath(XPATH_HELP_URL, HELP_URL);
        }
        return helpUrl;
    }

    public static String replaceHelpUrl(String string) {
        return string.replace(HELP_URL, getHelpUrl());
    }


    /**
     * The default constructor.
     *
     * @param id The ID of the state.
     * @param description The description of the state.
     */
    public AbstractState(String id, String description) {
        super();

        this.id          = id;
        this.description = description;
    }


    public AbstractState(String id, String description, String helpText) {
        this(id, description);
        this.helpText = replaceHelpUrl(helpText);
    }


    /**
     * Returns the ID of the state.
     *
     * @return the ID of the state.
     */
    public String getID() {
        return id;
    }


    /**
     * Set the ID of the state.
     *
     * @param id The ID of the state.
     */
    public void setID(String id) {
        this.id = id;
    }


    /**
     * Returns the description of the state.
     *
     * @return the description of the state.
     */
    public String getDescription() {
        return description;
    }


    /**
     * Set the description of the state.
     *
     * @param description The description of the state.
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Returns the help text of this state.
     *
     * @return the help text.
     */
    public String getHelpText() {
        return helpText;
    }


    /**
     * Set the help text for this state.
     *
     * @param helpText The help text.
     */
    public void setHelpText(String helpText) {
        this.helpText = replaceHelpUrl(helpText);
    }


    /**
     * Returns the data of the state.
     *
     * @return the data of the state.
     */
    public Map<String, StateData> getData() {
        return data;
    }


    /**
     * Returns a specific data object of the state.
     *
     * @param name The name of the data object.
     *
     * @return a data object of the state or null if no such data object exists.
     */
    public StateData getData(String name) {
        if (data != null) {
            return data.get(name);
        }

        return null;
    }


    /**
     * Add new data to the state. NOTE: If there is already an object existing
     * with the key <i>name</i>, this object is overwritten by the new value.
     *
     * @param name The name of the data object.
     * @param data The data object.
     */
    public void addData(String name, StateData data) {
        if (this.data == null) {
            this.data = new HashMap<String, StateData>();
        }

        this.data.put(name, data);
    }


    /**
     * Returns the list of possible outputs of this state. The list is empty
     * if no output is available for this state.
     *
     * @return a list of possible outputs of this state.
     */
    public List<Output> getOutputs() {
        return outputs;
    }


    /**
     * Initialize the state based on the state node in the configuration.
     *
     * @param config The state configuration node.
     */
    public void setup(Node config) {
        logger.info("AbstractState.setup");

        id = (String) XMLUtils.xpath(config, XPATH_ID, XPathConstants.STRING);

        description = (String) XMLUtils.xpath(
            config, XPATH_DESCRIPTION, XPathConstants.STRING);

        helpText = (String) XMLUtils.xpath(
            config, XPATH_HELP_TEXT, XPathConstants.STRING);

        if (helpUrl != null) {
            helpUrl = replaceHelpUrl(helpUrl);
        }

        setupOutputs(config);
    }


    /**
     * This default implementation does nothing at all.
     *
     * @param orig
     * @param owner
     * @param context
     * @param callMeta
     */
    public void initialize(
        Artifact orig,
        Artifact owner,
        Object   context,
        CallMeta callMeta
    ) {
        // do nothing.
    }


    /**
     * This method tries reading the available output nodes configured in the
     * state configuration and adds possible Outputs to the outputs list.
     *
     * @param config The state configuration node.
     */
    protected void setupOutputs(Node config) {
        NodeList outs = (NodeList) XMLUtils.xpath(
            config,
            XPATH_OUTPUT_MODES,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (outs == null || outs.getLength() == 0) {
            return;
        }

        int size = outs.getLength();

        for (int i = 0; i < size; i++) {
            addOutput(buildOutput(outs.item(i)));
        }
    }

    /**
     * This methods allows subclasses to manually add outputs
     *
     * @param out The output to add
     */
    protected void addOutput(Output out) {
        outputs.add(out);
    }

    /**
     * A helper method that creates an Output object based on the <i>out</i>
     * node.
     *
     * @param out The output node configuration.
     *
     * @return an Output object.
     */
    protected Output buildOutput(Node out) {
        String name = XMLUtils.xpathString(
            out, "@name", ArtifactNamespaceContext.INSTANCE);

        String desc = XMLUtils.xpathString(
            out, "@description", ArtifactNamespaceContext.INSTANCE);

        String mimetype = XMLUtils.xpathString(
            out, "@mime-type", ArtifactNamespaceContext.INSTANCE);

        String type = XMLUtils.xpathString(
            out, "@type", ArtifactNamespaceContext.INSTANCE);

        if (name == null) {
            return null;
        }

        NodeList facets = (NodeList) XMLUtils.xpath(
            out,
            XPATH_FACETS,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (facets == null || facets.getLength() == 0) {
            return new DefaultOutput(name, desc, mimetype, type);
        }

        int num = facets.getLength();

        List<Facet> facetList = new ArrayList<Facet>(num);

        for (int i = 0; i < num; i++) {
            Facet facet = buildFacet(facets.item(i));

            if (facet != null) {
                facetList.add(facet);
            }
        }

        return new DefaultOutput(name, desc, mimetype, facetList, type);
    }


    /**
     * A helper method that creates a Facet object based on the <i>facet</i>
     * node.
     *
     * @param facet The facet node.
     *
     * @return a Facet object or null if no valid Facet was found.
     */
    protected Facet buildFacet(Node facet) {
        String name = XMLUtils.xpathString(
            facet, "@name", ArtifactNamespaceContext.INSTANCE);

        String desc = XMLUtils.xpathString(
            facet, "@description", ArtifactNamespaceContext.INSTANCE);

        return name != null ? new DefaultFacet(name, desc) : null;
    }


    /**
     * Describes the UI of the state. This method needs to be implemented by
     * concrete subclasses.
     *
     * @param artifact A reference to the artifact this state belongs to.
     * @param document Describe doucment.
     * @param rootNode Parent node for all new elements.
     * @param context The CallContext.
     * @param uuid The uuid of an artifact.
     */
    public abstract Element describe(
        Artifact    artifact,
        Document    document,
        Node        rootNode,
        CallContext context,
        String      uuid
    );


    @Override
    public void endOfLife(Artifact artifact, Object context) {
        // nothing to do here
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
