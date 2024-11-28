/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts;

import java.io.Serializable;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface Message extends Serializable {

    String getText();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
