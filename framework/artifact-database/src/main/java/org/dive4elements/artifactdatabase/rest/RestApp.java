/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifacts.ArtifactDatabase;

import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;

import org.restlet.routing.Router;

/**
 * This is the core REST application that binds the several resources
 * used to manage the artifact database to the HTTP server provided
 * by the Restlet framework.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class RestApp
extends      Application
{
    /**
     * The central artifact database instance to work with.
     */
    protected ArtifactDatabase database;

    /**
     * Default constructor
     */
    public RestApp() {
    }

    public RestApp(Context context, ArtifactDatabase database) {
        super(context);
        this.database = database;
    }

    /**
     * Constructor to create REST appliction bound to a specific
     * artifact database.
     *
     * @param database The artifact database to be used.
     */
    public RestApp(ArtifactDatabase database) {
        this.database = database;
    }

    /**
     * Overwrites the createRoot() method of Application to
     * build the resource tree to form the exposed server URLs.
     *
     * @return The root of the URL tree exposed by the HTTP server.
     */
    @Override
    public Restlet createInboundRoot() {

        Context context = getContext();

        ConcurrentMap map = context.getAttributes();
        map.put("database", database);

        Router router = new Router(context);

        router.attach(ServicesResource.PATH,    ServicesResource.class);
        router.attach(ServiceResource.PATH,     ServiceResource.class);
        router.attach(FactoriesResource.PATH,   FactoriesResource.class);
        router.attach(CreateResource.PATH,      CreateResource.class);
        router.attach(ArtifactResource.PATH,    ArtifactResource.class);
        router.attach(ArtifactOutResource.PATH, ArtifactOutResource.class);
        router.attach(ExportResource.PATH,      ExportResource.class);
        router.attach(ImportResource.PATH,      ImportResource.class);
        router.attach(CreateUserResource.PATH,  CreateUserResource.class);
        router.attach(ListUsersResource.PATH,   ListUsersResource.class);
        router.attach(UserResource.PATH,        UserResource.class);
        router.attach(FindUserResource.PATH,    FindUserResource.class);
        router.attach(
            CreateCollectionResource.PATH, CreateCollectionResource.class);
        router.attach(
            ListCollectionsResource.PATH, ListCollectionsResource.class);
        router.attach(
            CollectionResource.PATH, CollectionResource.class);
        router.attach(
            CollectionOutResource.PATH, CollectionOutResource.class);

        return router;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
