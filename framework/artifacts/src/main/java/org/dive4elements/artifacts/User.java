/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts;

import org.w3c.dom.Document;

import java.io.Serializable;

public interface User
extends          Serializable
{
    String identifier();

    String getName();

    void setName(String name);

    void setIdentifier(String identifier);

    void setRole(Document role);

    Document getRole();

    String getAccount();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
