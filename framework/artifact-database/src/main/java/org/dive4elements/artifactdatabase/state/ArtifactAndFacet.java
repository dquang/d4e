package org.dive4elements.artifactdatabase.state;

import java.util.List;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.DataProvider;


/**
 * A bundle of a "native" Facet and its Artifact.
 */
public class ArtifactAndFacet implements DataProvider {
    /** The Artifact. */
    protected Artifact artifact;

    /** The (native) facet. */
    protected Facet    facet;

    /** An alternative facet description that might be set from outside. */
    protected String facetDescription;


    /** Trivial constructor. */
    public ArtifactAndFacet(
        Artifact a,
        Facet f
    ) {
        this.artifact   = a;
        this.facet      = f;
    }


    /** Get data (to plot). */
    public Object getData(CallContext context) {
        return facet.getData(artifact, context);
    }


    /** Get data (for other facet). */
    @Override
    public Object provideData(Object key, Object param, CallContext context) {
        return facet.provideBlackboardData(artifact, key, param, context);
    }


    /** (Maybe) Register on blackboard (depending on facet). */
    @Override
    public void register(CallContext context) {
        List keys = facet.getDataProviderKeys(this.artifact, context);
        if (keys == null) {
            return;
        }
        for (Object key: keys) {
            context.registerDataProvider(key, this);
        }
    }


    /** Access the artifact. */
    public Artifact getArtifact() {
        return artifact;
    }


    /** Access the (native) facet. */
    public Facet getFacet() {
        return facet;
    }


    /** Shortcut to facets name. */
    public String getFacetName() {
        return facet.getName();
    }


    /**
     * Returns the description for a facet. The return value depends on the
     * internal <i>facetDescription</i> instance variable. If this has been set
     * by setFacetDescription, this value is returned, otherwise the return
     * value of facet.getDescription().
     */
    public String getFacetDescription() {
        if (facetDescription == null) {
            return facet.getDescription();
        }

        return facetDescription;
    }


    public void setFacetDescription(String facetDescription) {
        this.facetDescription = facetDescription;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
