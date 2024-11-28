package org.dive4elements.artifactdatabase.state;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Attributes keep the order in which they were inserted.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultSection implements Section {

    protected String id;

    protected List<Section> subsections;

    /** Attribute-map. */
    protected Map<String, Attribute> attributes;


    /**
     * Creates a new DefaultSection instance. <b>Note, that the <i>id</i> is used
     * as Node name of the new Element that is created in toXML().</b>
     */
    public DefaultSection(String id) {
        this.id          = id;
        // Use LinkedHashMap to keep insertion order.
        this.attributes  = new LinkedHashMap<String, Attribute>();
        this.subsections = new ArrayList<Section>();
    }


    @Override
    public String getId() {
        return id;
    }


    @Override
    public void addSubsection(Section subsection) {
        if (subsection != null) {
            subsections.add(subsection);
        }
    }


    @Override
    public int getSubsectionCount() {
        return subsections.size();
    }


    @Override
    public Section getSubsection(int pos) {
        if (pos >= 0 && pos < getSubsectionCount()) {
            return subsections.get(pos);
        }

        return null;
    }


    /** Adding attribute to end of list. */
    @Override
    public void addAttribute(String key, Attribute attribute) {
        if (key != null && key.length() > 0 && attribute != null) {
            attributes.put(key, attribute);
        }
    }


    @Override
    public Attribute getAttribute(String key) {
        if (key == null || key.length() == 0) {
            return null;
        }

        return attributes.get(key);
    }


    @Override
    public Set<String> getKeys() {
        return attributes.keySet();
    }


    @Override
    public void toXML(Node parent) {
        Document owner     = parent.getOwnerDocument();
        Element  sectionEl = owner.createElement(getId());

        parent.appendChild(sectionEl);

        for (String key: getKeys()) {
            Attribute attr = getAttribute(key);
            attr.toXML(sectionEl);
        }

        for (int i = 0, n = getSubsectionCount(); i < n; i++) {
            Section subsection = getSubsection(i);
            subsection.toXML(sectionEl);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
