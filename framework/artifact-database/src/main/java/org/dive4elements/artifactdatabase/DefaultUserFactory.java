/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.User;
import org.dive4elements.artifacts.UserFactory;


/**
 * Default implementation of a UserFactory.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultUserFactory
implements   UserFactory
{
    /** The logger that is used in this factory.*/
    private static Logger logger = LogManager.getLogger(DefaultUserFactory.class);


    /**
     * Default constructor.
     */
    public DefaultUserFactory() {
    }


    public void setup(Document config, Node factoryNode) {
        logger.debug("DefaultUserFactory.setup");
    }


    /**
     * This method creates a new DefaultUser with the given identifier, name and
     * role.
     *
     * @param identifier The identifier for the new user.
     * @param name The name for the new user.
     * @param account The name of the new users account.
     * @param role The role for the new user.
     * @param context The CallContext.
     */
    public User createUser(
        String   identifier,
        String   name,
        String   account,
        Document role,
        Object   context)
    {
        logger.debug("DefaultUserFactory.createUser: " + name);
        return new DefaultUser(identifier, name, account, role);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
