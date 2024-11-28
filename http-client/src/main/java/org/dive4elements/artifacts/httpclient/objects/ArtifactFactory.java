/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.httpclient.objects;


public class ArtifactFactory {

    private String name;
    private String description;

    public ArtifactFactory(String name, String description) {
        this.name        = name;
        this.description = description;
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
