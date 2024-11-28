/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactNamespaceContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.data.MediaType;

import org.restlet.ext.xml.DomRepresentation;

import org.restlet.representation.Representation;

import org.restlet.resource.ResourceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Resource to list the available service offered by the artifact database.
 *
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class ServicesResource
extends      BaseResource
{
    private static Logger logger = LogManager.getLogger(ServicesResource.class);

    /**
     * server URL where to reach the resource.
     */
    public static final String PATH = "/services";

    @Override
    protected Representation innerGet()
    throws                   ResourceException
    {
        Document document = XMLUtils.newDocument();

        ElementCreator ec = new ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        ArtifactDatabase db = (ArtifactDatabase)getContext()
            .getAttributes().get("database");

        Element root = ec.create("result");
        document.appendChild(root);

        Element type = ec.create("type");
        ec.addAttr(type, "name", "service-list");
        root.appendChild(type);

        Element factories = ec.create("services");
        root.appendChild(factories);

        String [][] factoryNames = db.serviceNamesAndDescriptions();

        for (int i = 0; i < factoryNames.length; ++i) {
            String [] nd = factoryNames[i];
            Element factoryElement = ec.create("service");
            ec.addAttr(factoryElement, "name", nd[0]);
            ec.addAttr(factoryElement, "description", nd[1]);
            factories.appendChild(factoryElement);
        }

        document.normalizeDocument();

        return new DomRepresentation(
            MediaType.APPLICATION_XML, document);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
