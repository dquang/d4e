package org.dive4elements.artifactdatabase.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.data.StateData;


/**
 * The StateEngine stores all states and associated information about
 * outputs and facets for each Artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class StateEngine {

    /** The logger used in this class. */
    private static Logger logger = LogManager.getLogger(StateEngine.class);

    /**
     * A map that contains the states of the artifacts. The key of this map is
     * the name of an artifact, its value is a list of all states the artifact
     * can reach.
     */
    protected Map<String, List<State>> states;


    /**
     * A map that contains all existing states. The key of this map is the ID of
     * the state, its value is the state itself.
     */
    protected Map<String, State> allStates;


    /**
     * The default constructor.
     */
    public StateEngine() {
        states    = new HashMap<String, List<State>>();
        allStates = new HashMap<String, State>();
    }


    /**
     * This method adds a state into the map <i>allStates</i>.
     *
     * @param state The state to add.
     */
    protected void addState(State state) {
        allStates.put(state.getID(), state);
    }


    /**
     * Returns the state based on its ID.
     *
     * @param stateId The ID of the desired state.
     *
     * @return the state.
     */
    public State getState(String stateId) {
        return allStates.get(stateId);
    }


    public StateData getStateData(String artifact, String dataName) {
        List<State> artifactStates = getStates(artifact);

        if (artifactStates == null || artifactStates.size() == 0) {
            logger.warn("No States for Artifact '" + artifact + "' existing.");
            return null;
        }

        for (State state: artifactStates) {
            StateData sd = state.getData(dataName);

            if (sd != null) {
                return sd;
            }
        }

        logger.warn(
            "No StateData for Artifact '" + artifact +
            "' with name '" + dataName + "' existing.");

        return null;
    }


    /**
     * Add new states for a specific artifact.
     *
     * @param artifact The name of the artifact.
     * @param states A list of states that the artifact can reach.
     *
     * @return true, if the states were added, otherwise false.
     */
    public boolean addStates(String artifact, List<State> states) {
        List tmp = this.states.get(artifact);

        if (tmp != null) {
            logger.info(
                "States for the artifact '" + artifact + "' already stored.");

            return false;
        }

        // add the state to the map with all existing states
        for (State s: states) {
            addState(s);
        }

        logger.debug("Add new states for the artifact '" + artifact + "'");
        return this.states.put(artifact, states) != null;
    }


    /**
     * Returns the state list of an artifact specified by its name.
     *
     * @param artifact The name of the artifact (e.g. "winfo").
     *
     * @return the list of states of this artifact or <i>null</i> if no states
     * are existing for this <i>artifact</i>.
     */
    public List<State> getStates(String artifact) {
        return states.get(artifact);
    }


    /**
     * Return mapping of output to facets for an artifact in its states.
     */
    public Map<String, List<String>> getCompatibleFacets(List<String> aStates) {
        Map<String, List<String>> compatibilityMatrix =
            new HashMap<String, List<String>>();

        // For all states that the artifact had seen, add outputs facets.
        logger.debug("Searching in " + aStates);
        for (String stateId: aStates) {

            State state = allStates.get(stateId);
            if (state == null) {
                logger.debug("No state found for id " + stateId);
                continue;
            }

            for (Output output: state.getOutputs()) {
                List<Facet> outFacets = output.getFacets();
                logger.debug("Facets for output " + output.getName() + " :" + outFacets);

                List<String> oldFacets = compatibilityMatrix.get(output.getName());

                if (oldFacets == null) {
                    oldFacets = new ArrayList<String>();
                }

                for (Facet facet: outFacets) {
                    oldFacets.add(facet.getName());
                }

                compatibilityMatrix.put(output.getName(), oldFacets);
            }
        }
        return compatibilityMatrix;
    }
}
// vim:set ts=4 sw=4 et sta sts=4 fenc=utf8 :
