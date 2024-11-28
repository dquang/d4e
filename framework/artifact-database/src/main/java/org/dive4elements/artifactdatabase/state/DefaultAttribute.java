package org.dive4elements.artifactdatabase.state;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultAttribute implements Attribute {

    protected String name;

    protected Object value;


    public DefaultAttribute(String name, Object value) {
        this.name  = name;
        this.value = value;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public Object getValue() {
        return value;
    }


    @Override
    public void setValue(Object value) {
        this.value = value;
    }


    @Override
    public Node toXML(Node parent) {
        Document owner = parent.getOwnerDocument();
        Element   attr = owner.createElement(getName());

        parent.appendChild(attr);

        attr.setTextContent(String.valueOf(getValue()));

        return attr;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
