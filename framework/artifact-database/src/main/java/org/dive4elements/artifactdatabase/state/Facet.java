package org.dive4elements.artifactdatabase.state;

import java.util.List;
import java.util.Map;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Facet extends Serializable {

    /**
     * Returns the index of this facet.
     *
     * @return the index of this facet.
     */
    int getIndex();

    /**
     * Returns the name of this facet.
     *
     * @return the name of this facet.
     */
    String getName();

    /**
     * Returns the name of the out this facet is bound to.
     *
     * @return the name of the out this facet is bound to.
     */
    String getBoundToOut();

    /**
     * Binds this facet to an out.
     */
    void setBoundToOut(String value);

    /**
     * Returns the description of this facet.
     *
     * @return the description of this facet.
     */
    String getDescription();


    /**
     * Returns the data this facet requires.
     *
     * @param artifact The owner artifact.
     * @param context The CallContext.
     *
     * @return the data.
     */
    Object getData(Artifact artifact, CallContext context);


    /**
     * Returns the meta data this facet provides.
     *
     * @return the meta data.
     */
    Map<String, String> getMetaData();

    /**
     * Returns the meta data this facet provides.
     *
     * @param artifact The owner artifact.
     * @param context The CallContext.
     *
     * @return the meta data.
     */
    Map<String, String> getMetaData(Artifact artifact, CallContext context);

    /**
     * Add a key value pair to the facets metadata.
     *
     * @param key   The meta data key.
     * @param value The meta data value.
     */
    void addMetaData(String key, String value);

    /**
     * Get keys for which this Facet can provide data (for other facets, not
     * for plot).
     * @param artifact Artifact that this facet belongs to.
     * @return list of keys
     */
    List getDataProviderKeys(Artifact artifact, CallContext context);


    /**
     * Provide data to other facet.
     *
     * @param art  The artifact that this facet belongs to.
     * @param key  the key of the requested service.
     * @param prm  optional parameters.
     * @param ctxt the callcontext.
     *
     * @return the data
     */
    Object provideBlackboardData(
        Artifact art,
        Object key,
        Object prm,
        CallContext ctxt);


    /**
     * Write the internal representation of a facet to a node.
     *
     * @param doc A Document.
     *
     * @return the representation as Node.
     */
    Node toXML(Document doc);

    Facet deepCopy();

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
