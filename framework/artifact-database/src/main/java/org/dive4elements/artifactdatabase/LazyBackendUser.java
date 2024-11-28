/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.User;
import org.dive4elements.artifacts.UserFactory;

import org.w3c.dom.Document;

public class LazyBackendUser
implements   User
{
    protected UserFactory factory;
    protected Backend     backend;
    protected String      identifier;
    protected User        user;
    protected Object      context;

    public LazyBackendUser(
        String      identifier,
        UserFactory factory,
        Backend     backend,
        Object      context
    ) {
        this.identifier = identifier;
        this.factory    = factory;
        this.backend    = backend;
        this.context    = context;
    }

    protected User getUser() {
        if (user == null) {
            user = backend.getUser(identifier, factory, context);
            if (user == null) {
                throw new IllegalStateException("loading user failed");
            }
        }
        return user;
    }

    @Override
    public String identifier() {
        return getUser().identifier();
    }

    @Override
    public String getName() {
        return getUser().getName();
    }

    @Override
    public void setName(String name) {
        getUser().setName(name);
    }

    @Override
    public void setIdentifier(String identifier) {
        getUser().setIdentifier(identifier);
    }

    @Override
    public Document getRole() {
        return getUser().getRole();
    }

    @Override
    public void setRole(Document document) {
        getUser().setRole(document);
    }

    @Override
    public String getAccount() {
        return getUser().getAccount();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
