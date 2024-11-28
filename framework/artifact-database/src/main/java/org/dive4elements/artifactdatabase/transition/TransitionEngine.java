package org.dive4elements.artifactdatabase.transition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifactdatabase.state.StateEngine;


/**
 * The TransitionEngine stores all transitions for each Artifact and should be
 * used to determine, if an Artifact is able to advance from one to another
 * state.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class TransitionEngine {

    /** The logger used in this class. */
    private static Logger logger = LogManager.getLogger(TransitionEngine.class);

    /**
     * A map that contains the transitions of the artifacts. The key is the name
     * of the artifact, its value is a list of all transitions of this artifact.
     */
    protected Map<String, List> transitions;


    /**
     * The default constructor.
     */
    public TransitionEngine() {
        transitions = new HashMap<String, List>();
    }


    /**
     * Add new transitions for a specific artifact.
     *
     * @param stateId the name of the Artifact.
     * @param transition the list of transition of the artifact.
     *
     * @return true, if the transitions were added, otherwise false.
     */
    public boolean addTransition(String stateId, Transition transition) {
        List tmp = transitions.get(stateId);

        if (tmp == null) {
            tmp = new ArrayList<Transition>();
        }

        tmp.add(transition);

        logger.debug("Add new transitions for state '" + stateId + "'");

        return transitions.put(stateId, tmp) != null;
    }


    /**
     * This method returns all existing transitions of a state.
     *
     * @param state The state
     *
     * @return the existing transition of <i>state</i>.
     */
    public List<Transition> getTransitions(State state) {
        return transitions.get(state.getID());
    }


    /**
     * This method returns the reachable states of <i>state</i>.
     *
     * @param state The current state.
     * @param engine The state engine.
     *
     * @return a list of reachable states.
     */
    public List<State> getReachableStates(
        Artifact    artifact,
        State       state,
        StateEngine engine) {
        List<Transition> transitions = getTransitions(state);
        List<State>      reachable   = new ArrayList<State>();

        if (transitions == null) {
            return reachable;
        }

        for (Transition t: transitions) {
            State target = engine.getState(t.getTo());

            if (t.isValid(artifact, state, target)) {
                reachable.add(target);
            }
        }

        return reachable;
    }

    /** Returns all recursive reachable state ids for
     *  a given pair of artifact id and state id.
     */
    public Set<String> allRecursiveSuccessorStateIds(
        String artifactIdentifier,
        String stateId
    ) {
        HashSet<String> result = new HashSet<String>();

        List<Transition> trans = transitions.get(artifactIdentifier);

        if (trans == null) {
            return result;
        }

        Map<String, Set<String>> succs = new HashMap<String, Set<String>>();

        for (Transition t: trans) {
            String from = t.getFrom();
            String to   = t.getTo();

            Set<String> s = succs.get(from);
            if (s == null) {
                s = new HashSet<String>();
                succs.put(from, s);
            }
            s.add(to);
        }

        Set<String> start = succs.get(stateId);

        if (start == null) {
            return result;
        }

        Deque<String> open = new ArrayDeque<String>(start);

        while (!open.isEmpty()) {
            String cand = open.pop();
            if (result.add(cand)) {
                Set<String> s = succs.get(cand);
                if (s != null) {
                    open.addAll(s);
                }
            }
        }

        return result;
    }


    /**
     * Determines if a state with a given identifier is reachable from a current
     * state.
     *
     * @param artifact The owner artifact of state <i>state</i>.
     * @param targetId The identifier of the target state.
     * @param state The start state.
     * @param stateEngine The StateEngine.
     *
     * @return true, if the target state is reachable, otherwise false.
     */
    public boolean isStateReachable(
        Artifact    artifact,
        String      targetId,
        State       state,
        StateEngine stateEngine)
    {
        List<State> reachable = getReachableStates(artifact, state,stateEngine);

        if (reachable == null || reachable.size() == 0) {
            return false;
        }

        for (State s: reachable) {
            if (targetId.equals(s.getID())) {
                return true;
            }
        }

        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
