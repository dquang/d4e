/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.CollectionItem;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.w3c.dom.Document;

public class DefaultCollectionItem
implements   CollectionItem
{
    protected String artifactIdentifier;

    protected byte [] data;

    protected Document document;

    public DefaultCollectionItem() {
    }

    public DefaultCollectionItem(String artifactIdentifier, byte [] attribute) {
        this.artifactIdentifier = artifactIdentifier;
        this.data               = attribute;
    }

    public String getArtifactIdentifier() {
        return artifactIdentifier;
    }

    public synchronized Document getAttribute() {
        if (document == null) {
            if (data != null) {
                document = XMLUtils.fromByteArray(data, true);
            }
        }
        return document;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
