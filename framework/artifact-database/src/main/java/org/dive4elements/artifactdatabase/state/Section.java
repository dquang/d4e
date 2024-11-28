package org.dive4elements.artifactdatabase.state;

import java.io.Serializable;
import java.util.Set;

import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Section extends Serializable {

    /**
     * Returns an ID for this Section.
     *
     * @return an ID for this Section.
     */
    String getId();

    /**
     * Adds a new subsection to this Section object.
     *
     * @param subsection the new Section.
     */
    void addSubsection(Section subsection);

    /**
     * Returns the number of subsections in this Section.
     *
     * @return the number of subsections.
     */
    int getSubsectionCount();

    /**
     * Returns a subsection at position <i>pos</i>.
     *
     * @param pos The position of the target subsection.
     *
     * @return the subsection at position <i>pos</i>.
     */
    Section getSubsection(int pos);

    /**
     * Adds a new Attribute to this Section.
     *
     * @param key The key that is used to store/retrieve the Attribute.
     * @param attribute The new Attribute.
     */
    void addAttribute(String key, Attribute attribute);

    /**
     * Returns an Attribute for the specified <i>key</i>.
     *
     * @param key The key that is used to retrieve the target Attribute.
     *
     * @return the Attribute specified by <i>key</i>.
     */
    Attribute getAttribute(String key);

    /**
     * Returns all keys of all Attributes currently stored in this Section.
     *
     * @return all keys of all Attributes.
     */
    Set<String> getKeys();

    /**
     * Transforms this Section into XML using Attribute.toXML() for each
     * Attribute and Section.toXML() for each subsection stored in this Section.
     *
     * @param parent The parent node.
     */
    void toXML(Node parent);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
