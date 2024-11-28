package org.dive4elements.artifactdatabase.state;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultSettings implements Settings {

    protected List<Section> sections;

    public DefaultSettings() {
        sections = new ArrayList<Section>();
    }

    @Override
    public void addSection(Section section) {
        if (section != null) {
            sections.add(section);
        }
    }

    @Override
    public int getSectionCount() {
        return sections.size();
    }

    @Override
    public Section getSection(int pos) {
        if (pos >= 0 && pos < getSectionCount()) {
            return sections.get(pos);
        }

        return null;
    }

    @Override
    public void removeSection(Section section) {
        if (section != null) {
            sections.remove(section);
        }
    }

    @Override
    public void toXML(Node parent) {
        Document owner    = parent.getOwnerDocument();
        Element  settings = owner.createElement("settings");

        for (Section section: sections) {
            section.toXML(settings);
        }

        parent.appendChild(settings);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
