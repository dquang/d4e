package org.dive4elements.artifacts.common.model;

/**
 * The default implementation of the {@link User} interface.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultUser implements User {

    /** The user's firstname. */
    protected String firstName;

    /** The user's lastname. */
    protected String lastName;


    /**
     * Creates an empty user without name.
     */
    public DefaultUser() {
    }


    /**
     * Creates a user with first and lastname.
     *
     * @param firstName The user's firstname.
     * @param lastName The user's lastname.
     */
    public DefaultUser(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName  = lastName;
    }


    /**
     * This method returns the firstname of the user.
     *
     * @return the firstname.
     */
    public String getFirstName() {
        return firstName;
    }


    /**
     * Sets the user's firstname.
     *
     * @param firstName The user's firstname.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    /**
     * This method returns the lastname of the user.
     *
     * @return the lastname.
     */
    public String getLastName() {
        return lastName;
    }


    /**
     * Sets the user's lastname.
     *
     * @param lastName The user's lastname.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
