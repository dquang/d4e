package org.dive4elements.artifactdatabase.state;

import java.util.List;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Output {

    /**
     * Retrieve the name of this output mode.
     *
     * @return the name of this output mode.
     */
    public String getName();

    /**
     * Retrieve the description of an output.
     *
     * @return the description.
     */
    public String getDescription();

    /**
     * Retrieve the mimetype used for the output.
     *
     * @return the mimetype.
     */
    public String getMimeType();


    /**
     * Returns the type of this output.
     *
     * @return the type.
     */
    public String getType();

    /**
     * Retrieve the facets of this output.
     *
     * @return the facets of this output.
     */
    public List<Facet> getFacets();

    /**
     * Add a new facet to this output.
     *
     * @param facet The new facet.
     */
    public void addFacet(Facet facet);

    /**
     * Add a list of facet to this output.
     *
     * @param facets A list of facets.
     */
    public void addFacets(List<Facet> facets);

    /**
     * Replaces the old list of facets with a new one.
     *
     * @param facets A list of new facets.
     */
    public void setFacets(List<Facet> facets);

    /**
     * Returns a Settings object for this Output.
     */
    public Settings getSettings();

    /**
     * Sets the Settings for this Output.
     *
     * @param settings the Settings for this Output.
     */
    public void setSettings(Settings settings);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
