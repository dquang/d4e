package org.dive4elements.artifactdatabase.transition;

import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifactdatabase.state.State;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Transition {

    /**
     * Initializes the transition.
     *
     * @param config The configuration node for the transition.
     */
    public void init(Node config);

    /**
     * Return the ID of the start State.
     */
    public String getFrom();

    /**
     * Return the ID of the target State.
     */
    public String getTo();

    /**
     * Set the ID of the current State.
     *
     * @param from The ID of the current state.
     */
    public void setFrom(String from);

    /**
     * Set the ID of the target State.
     *
     * @param to The ID of the target state.
     */
    public void setTo(String to);

    /**
     * Determines if its valid to step from state <i>a</i> of an artifact
     * <i>artifact</i> to state <i>b</i>.
     *
     * @param artifact The owner artifact of state a and b.
     * @param a The current state.
     * @param b The target state.
     *
     * @return true, if it is valid to step from a to b, otherwise false.
     */
    public boolean isValid(Artifact artifact, State a, State b);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
