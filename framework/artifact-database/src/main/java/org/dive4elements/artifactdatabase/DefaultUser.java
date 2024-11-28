/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.User;


/**
 * Trivial implementation of a user. Useful to be subclassed.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultUser
implements   User
{
    /** The identifier of the user.*/
    protected String identifier;

    /** The name of the user.*/
    protected String name;

    /** The account name of the user.*/
    protected String account;

    /** The role of the user.*/
    protected Document role;


    /**
     * The default constructor.
     */
    public DefaultUser() {
    }

    public DefaultUser(String identifier) {
        this.identifier = identifier;
    }

    /**
     * A constructor that creates a new user.
     *
     * @param identifier The uuid of the user.
     * @param name The name of the user.
     * @param account The account name of the user.
     * @param role The role of the user.
     */
    public DefaultUser(String identifier, String name, String account,
                       Document role) {
        this.identifier = identifier;
        this.name       = name;
        this.role       = role;
        this.account    = account;
    }


    /**
     * Returns the identifier of this user.
     *
     * @return the identifier of this user.
     */
    @Override
    public String identifier() {
        return identifier;
    }


    /**
     * Returns the name of the user.
     *
     * @return the name of the user.
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Set the name of the user.
     *
     * @param name The name for this user.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Set the identifier of the user.
     *
     * @param identifier The new identifier.
     */
    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    /**
     * Set the role of the user.
     *
     * @param role The new role of the user.
     */
    @Override
    public void setRole(Document role) {
        this.role = role;
    }


    /**
     * Returns the role of the user.
     *
     * @return the role of the user.
     */
    @Override
    public Document getRole() {
        return role;
    }

    /**
     * Returns the account of the user.
     *
     * @return the account name of the user.
     */
    @Override
    public String getAccount() {
        return account;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
