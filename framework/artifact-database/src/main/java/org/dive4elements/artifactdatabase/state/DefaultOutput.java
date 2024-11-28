package org.dive4elements.artifactdatabase.state;

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of an Output.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultOutput implements Output {

    protected String name;

    protected String description;

    protected String mimeType;

    protected String type;

    protected List<Facet> facets;

    protected Settings settings;


    /**
     * The default constructor that instantiates a new DefaultOutput object.
     *
     * @param name The name of this output.
     * @param description The description of this output.
     * @param mimeType The mimetype of this output.
     */
    public DefaultOutput(String name, String description, String mimeType) {
        this.name        = name;
        this.description = description;
        this.mimeType    = mimeType;
        this.type        = "";
        this.facets      = new ArrayList<Facet>();
    }


    public DefaultOutput(
        String      name,
        String      description,
        String      mimeType,
        String      type)
    {
        this(name, description, mimeType);

        this.facets = new ArrayList<Facet>();
        this.type   = type;
    }


    public DefaultOutput(
        String      name,
        String      description,
        String      mimeType,
        List<Facet> facets)
    {
        this(name, description, mimeType);

        this.type   = "";
        this.facets = facets;
    }


    /**
     * This constructor builds a new Output object that contains facets as well.
     *
     * @param name The name of this output.
     * @param description The description of this output.
     * @param mimeType The mimetype of this output.
     * @param facets The list of facets supported by this output.
     * @param type The type of the Output e.g. chart
     */
    public DefaultOutput(
        String      name,
        String      description,
        String      mimeType,
        List<Facet> facets,
        String      type)
    {
        this(name, description, mimeType, facets);

        this.type = type;
    }


    /**
     * Returns the name of this output.
     *
     * @return the name of this output.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the description of this output.
     *
     * @return the description of this output.
     */
    public String getDescription() {
        return description;
    }


    /**
     * Returns the mimetype of this output.
     *
     * @return the mimetype of this output.
     */
    public String getMimeType() {
        return mimeType;
    }


    public String getType() {
        return type;
    }


    /**
     * Returns the list of facets supported by this output.
     *
     * @return the list of facets supported by this output.
     */
    public List<Facet> getFacets() {
        return facets;
    }


    public void addFacet(Facet facet) {
        if (facet != null && !facets.contains(facet)) {
            facets.add(facet);
        }
    }


    public void addFacets(List<Facet> facets) {
        if (facets != null) {
            this.facets.addAll(facets);
        }
    }


    @Override
    public void setFacets(List<Facet> facets) {
        this.facets = facets;
    }


    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
    }


    @Override
    public Settings getSettings() {
        return settings;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
