package org.dive4elements.artifactdatabase.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;


/**
 * The default implementation of a Facet.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultFacet implements Facet {

    /** The index of this facet. */
    protected int index;

    /** The name of this facet. */
    protected String name;

    /** The out this facet is bound to. */
    protected String boundToOut;

    /** The description of this facet. */
    protected String description;

    /** The meta data this facet provides. */
    protected Map<String, String> metaData;

    /** Trivial, empty constructor. */
    public DefaultFacet() {
        this.metaData = new HashMap<String, String>();
    }


    /**
     * The default constructor to create new Facet objects.
     *
     * @param name The name of this new facet.
     * @param description The description of this new facet.
     */
    public DefaultFacet(String name, String description) {
        this(0, name, description);
    }


    /**
     * The default constructor to create new Facet objects.
     *
     * @param index The index of this new facet.
     * @param name The name of this new facet.
     * @param description The description of this new facet.
     */
    public DefaultFacet(int index, String name, String description) {
        this.index       = index;
        this.name        = name;
        this.description = description;
        this.metaData    = new HashMap<String, String>();
    }


    /** Get index. */
    public int getIndex() {
        return index;
    }


    /** Returns the name ('type'). */
    public String getName() {
        return name;
    }


    /** Returns the description (e.g. displayed in gui). */
    public String getDescription() {
        return description;
    }


    /**
     * Returns the name of the out this facet is bound to.
     *
     * @return the name of the out this facet is bound to.
     */
    public String getBoundToOut() {
        return boundToOut;
    }


    /**
     * Binds this facet to an out.
     */
    public void setBoundToOut(String value) {
        boundToOut = value;
    }


    /**
     * @return null
     */
    public Object getData(Artifact artifact, CallContext context) {
        return null;
    }


    /**
     * Returns the meta data this facet provides.
     *
     * @param artifact The owner artifact.
     * @param context The CallContext.
     *
     * @return the meta data.
     */
    @Override
    public Map<String, String> getMetaData(
        Artifact artifact,
        CallContext context)
    {
        return this.metaData;
    }

    @Override
    public Map<String, String> getMetaData() {
        return this.metaData;
    }

    @Override
    public void addMetaData(String key, String value) {
        this.metaData.put(key, value);
    }

    /**
     * (Do not) provide data.
     * Override to allow other facets to access your data.
     * @return always null.
     */
    public Object provideBlackboardData(
        Artifact artifact,
        Object key,
        Object param,
        CallContext context
    ) {
        return null;
    }


    /*
     * Return list of keys (objects) for which this facet can provide data
     * ("external parameterization"), for other facets, via blackboard.
     * These are the keys that are independent from the current call (thus
     * 'static').
     * @param artifact that this facet belongs to.
     */
    public List getStaticDataProviderKeys(Artifact artifact) {
        return null;
    }

    /**
     * Return list of keys (objects) for which this facet can provide data
     * ("external parameterization"), for other facets, via blackboard.
     * @param artifact that this facet belongs to.
     */
    public List getDataProviderKeys(Artifact artifact, CallContext context) {
        return getStaticDataProviderKeys(artifact);
    }


    /** Create a xml represantation. */
    public Node toXML(Document doc) {
        ElementCreator ec = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element facet = ec.create("facet");
        ec.addAttr(facet, "description", description, true);
        ec.addAttr(facet, "name", name, true);
        ec.addAttr(facet, "index", String.valueOf(index), true);
        ec.addAttr(facet, "boundToOut", boundToOut, true);

        return facet;
    }


    /** Create a string representation. */
    public String toString() {
        return new StringBuilder("name = '")
            .append(name).append("', index = ")
            .append(index).append(", description = '")
            .append(description).append("', bound_out = '")
            .append(boundToOut).append("'")
            .toString();
    }


    /**
     * Copies name, index and description of other facet.
     */
    public void set(Facet other) {
        index       = other.getIndex();
        name        = other.getName();
        description = other.getDescription();
        boundToOut  = other.getBoundToOut();
        // FIXME: metadata ist NOT immutable, but a reference is simply copied during a 'deep' copy operation...
        metaData    = other.getMetaData();
    }


    /** Create a deep copy of this facet. */
    public Facet deepCopy() {
        DefaultFacet copy = new DefaultFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
