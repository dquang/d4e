/*
 * Copyright (c) 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase.data;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultStateData implements StateData {

    /** The name of the data. */
    protected String name;

    /** The description of the data. */
    protected String description;

    /** The type of the data. */
    protected String type;

    /** The value. */
    protected Object value;

    public DefaultStateData() {
    }

    /**
     * The default constructor. It creates empty StateData objects with no
     * value.
     *
     * @param name The name.
     * @param description The description.
     * @param type The type.
     */
    public DefaultStateData(String name, String description, String type) {
        this.name        = name;
        this.description = description;
        this.type        = type;
    }

    public void set(StateData other) {
        name        = other.getName();
        description = other.getDescription();
        type        = other.getType();
        value       = other.getValue();
    }


    /**
     * A constructor that takes the name of the data, its value and the
     * describing parameters description and type.
     *
     * @param name The name of the data item.
     * @param description The description.
     * @param type The type.
     * @param value The value of the data item.
     */
    public DefaultStateData(
        String name,
        String description,
        String type,
        String value)
    {
        this.name        = name;
        this.description = description;
        this.type        = type;
        this.value       = value;
    }


    /**
     * Returns the name of the data object.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the description of the data object.
     *
     * @return the description of the data object.
     */
    public String getDescription() {
        return description;
    }


    /**
     * Returns the type of the data object as string.
     *
     * @return the type as string.
     */
    public String getType() {
        return type;
    }


    /**
     * Returns the value of the data object.
     *
     * @return the value.
     */
    public Object getValue() {
        return value;
    }


    /**
     * Set the value of this data object.
     *
     * @param value The new value for this data object.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public StateData deepCopy() {
        DefaultStateData copy = new DefaultStateData();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
