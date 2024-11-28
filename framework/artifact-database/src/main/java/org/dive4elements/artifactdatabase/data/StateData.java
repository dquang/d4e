/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase.data;

import java.io.Serializable;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface StateData extends Serializable {

    /**
     * Returns the name of the data object.
     *
     * @return the name.
     */
    public String getName();


    /**
     * Returns the description of the data object.
     *
     * @return the description of the data object.
     */
    public String getDescription();


    /**
     * Returns the type of the data object as string.
     *
     * @return the type as string.
     */
    public String getType();


    /**
     * Returns the value of the data object.
     *
     * @return the value.
     */
    public Object getValue();


    /**
     * Set the value of this data object.
     *
     * @param value The new value for this data object.
     */
    public void setValue(Object value);

    public StateData deepCopy();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
