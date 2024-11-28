/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.PreferredLocale;

import java.util.Locale;

/**
 * Models a pair of Locale and quality (0.0-1.0) to be used to
 * find best matching locale between server offerings and clients requests.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultPreferredLocale
implements   PreferredLocale
{
    /**
     * The locale of this tuple pair.
     */
    protected Locale locale;
    /**
     * The quality of this tuple pair between 0.0 and 1.0.
     */
    protected float  quality;

    /**
     * Default constructor
     */
    public DefaultPreferredLocale() {
    }

    /**
     * Constructor to build a pair of given a locale speficied by
     * string 'lang' and an given 'quality'.
     * @param lang The name of the locale.
     * @param quality The quality of the locale.
     */
    public DefaultPreferredLocale(String lang, float quality) {
        locale = new Locale(lang);
        this.quality = quality;
    }

    public Locale getLocale() {
        return locale;
    }

    public float getQuality() {
        return quality;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
