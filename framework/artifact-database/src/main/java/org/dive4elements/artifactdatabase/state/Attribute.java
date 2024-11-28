package org.dive4elements.artifactdatabase.state;

import java.io.Serializable;

import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Attribute extends Serializable {

    /**
     * Returns the name of this Attribute.
     *
     * @return the name of this Attribute.
     */
    String getName();

    /**
     * Returns the value of this Attribute.
     *
     * @return the value of this Attribute.
     */
    Object getValue();

    /**
     * Sets the value of this Attribute.
     *
     * @param value The new value.
     */
    void setValue(Object value);

    /**
     * Transforms this Attribute into XML.
     *
     * @param parent The parent node.
     *
     * @return the Node that represents this Attribute.
     */
    Node toXML(Node parent);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
