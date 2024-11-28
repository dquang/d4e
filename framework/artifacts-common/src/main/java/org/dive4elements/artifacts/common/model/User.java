package org.dive4elements.artifacts.common.model;

import java.io.Serializable;

/**
 * An interface that describes a user of the system.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface User extends Serializable {

    /**
     * This method returns the firstname of the user.
     *
     * @return the firstname.
     */
    public String getFirstName();


    /**
     * Sets the user's firstname.
     *
     * @param firstName The user's firstname.
     */
    public void setFirstName(String firstName);


    /**
     * This method returns the lastname of the user.
     *
     * @return the lastname.
     */
    public String getLastName();


    /**
     * Sets the user's lastname.
     *
     * @param lastName The user's lastname.
     */
    public void setLastName(String lastName);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
