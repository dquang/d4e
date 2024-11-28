/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.common.utils;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;


/**
 *  @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapXPathVariableResolver implements XPathVariableResolver {

    protected Map<String, String> variables;


    public MapXPathVariableResolver() {
        this.variables = new HashMap<String, String>();
    }


    public MapXPathVariableResolver(Map<String, String> variables) {
        this.variables = variables;
    }


    public void addVariable(String name, String value) {
        if (name != null && value != null) {
            variables.put(name, value);
        }
    }


    @Override
    public Object resolveVariable(QName variableName) {
        String key = variableName.getLocalPart();
        return variables.get(key);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
