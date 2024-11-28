/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase.state;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.data.StateData;


/**
 * This interface describes the basic methods a concrete state class needs to
 * implement.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface State extends Serializable {

    /**
     * Return the id of the state.
     *
     * @return the id.
     */
    public String getID();


    /**
     * Return the description of the state.
     *
     * @return the description of the state.
     */
    public String getDescription();


    /**
     * Returns the help text configured for the state.
     *
     * @return the help text configured for the state.
     */
    public String getHelpText();


    /**
     * Returns the data provided by this state.
     *
     * @return the data stored in this state.
     */
    public Map<String, StateData> getData();


    /**
     * Returns a single desired StateData object based on its name.
     *
     * @param name The name of the desired StateData object.
     *
     * @return the desired StateData object.
     */
    public StateData getData(String name);


    /**
     * This method should be used to add a new {@link StateData} object to the
     * data pool of the state.
     *
     * @param name The name of the data object.
     * @param data The data object.
     */
    public void addData(String name, StateData data);


    /**
     * Returns the list of possible outputs of this state. The list is empty
     * if no output is available for this state.
     *
     * @return a list of possible outputs of this state.
     */
    public List<Output> getOutputs();


    /**
     * Initialize the state based on the state node in the configuration.
     *
     * @param config The state configuration node.
     */
    public void setup(Node config);


    /**
     * Initializes the internal state of this State based on an other State.
     *
     * @param orig The owner Artifact or the original State.
     * @param owner The owner Artifact of this State.
     * @param context The context object.
     * @param callMeta The CallMeta of the current call.
     */
    public void initialize(
        Artifact orig,
        Artifact owner,
        Object   context,
        CallMeta callMeta);


    /**
     * This method is called when an artifacts retrieves a describe request. It
     * creates the user interface description of the current state.
     *
     * @param artifact A reference to the artifact this state belongs to.
     * @param document Describe doucment.
     * @param rootNode Parent node for all new elements.
     * @param context The CallContext.
     * @param uuid The uuid of an artifact.
     */
    public Element describe(
        Artifact    artifact,
        Document    document,
        Node        rootNode,
        CallContext context,
        String      uuid
    );


    /**
     * This method should be called by an Artifact that removes this State
     * (current State and previous States). E.g. this might be interesting to
     * remove generated files or stuff like that.
     *
     * @param artifact A parent Artifact.
     * @param context The CallContext.
     */
    public void endOfLife(Artifact artifact, Object context);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
