package org.dive4elements.artifactdatabase.state;

import java.io.Serializable;

import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Settings extends Serializable {

    /**
     * Adds a new Section to this Settings object.
     *
     * @param section the new Section.
     */
    void addSection(Section section);

    /**
     * Returns the number of Sections in this Settings object.
     *
     * @return the number of sections.
     */
    int getSectionCount();

    /**
     * Returns the section at position <i>pos</i>.
     *
     * @param pos the position of the target Section.
     *
     * @return the Section at position <i>pos</i> or null if no Section is
     * existing at <i>pos</i>.
     */
    Section getSection(int pos);

    /**
     * Removes a Section if it is existing in this Settings.
     *
     * @param section The section that should be removed.
     */
    void removeSection(Section section);

    /**
     * Transforms this Settings object into a XML representation. Therefore,
     * each Section object's <i>toXML</i> method is called to append its XML
     * representation to the final document.
     *
     * @param parent The parent node.
     */
    void toXML(Node parent);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
