/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A factory which an XML in/XML out service which reachable through the
 * artifact database.
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface ServiceFactory
extends          Serializable
{
    /**
     * The name of the service which is created by this factory.
     * @return The name of the created service.
     */
    String getName();

    /**
     * The description of the service which is created by this factory.
     * @return The description.
     */
    String getDescription();

    /**
     * Creates the service. This is done at startup time of the
     * artifact database system.
     * @param globalContext The global context of the artifact database.
     * @return The created service.
     */
    Service createService(GlobalContext globalContext);

    /**
     * Configures this factory. This is called before
     * #createService(Object).
     * @param config The global configuration document of the artifact
     * database system.
     * @param factoryNode The node inside the configuration document which
     * corresponds to this factory.
     */
    void setup(Document config, Node factoryNode);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
