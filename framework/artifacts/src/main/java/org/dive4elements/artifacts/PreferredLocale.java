/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import java.util.Locale;

/**
 * Interface to build pairs of preference and quality.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface PreferredLocale
{
    /**
     * Returns the locale of the pair.
     * @return The locale.
     */
    Locale getLocale();
    /**
     * Returns the quality of the pair.
     * @return the quality
     */
    float getQuality();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
