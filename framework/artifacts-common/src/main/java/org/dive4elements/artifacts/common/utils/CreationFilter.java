package org.dive4elements.artifacts.common.utils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Element;

public class CreationFilter
{
    public static class Facet {

        protected String name;
        protected String index;

        public Facet() {
        }

        public Facet(String name, String index) {
            this.name  = name;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public String getIndex() {
            return index;
        }
    }

    protected Map<String, List<Facet>> outs;

    public CreationFilter() {
        outs = new HashMap<String, List<Facet>>();
    }

    public void add(String out, List<Facet> facets) {
        outs.put(out, facets);
    }

    public Element toXML(XMLUtils.ElementCreator ec) {
        Element filter = ec.create("filter");

        for (Map.Entry<String, List<Facet>> entry: outs.entrySet()) {
            Element out = ec.create("out");
            out.setAttribute("name", entry.getKey());
            for (Facet facet: entry.getValue()) {
                Element f = ec.create("facet");
                f.setAttribute("name", facet.getName());
                f.setAttribute("index", facet.getIndex());
                out.appendChild(f);
            }
            filter.appendChild(out);
        }

        return filter;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
