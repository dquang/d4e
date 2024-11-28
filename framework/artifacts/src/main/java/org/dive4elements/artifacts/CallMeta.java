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
 * Interface to inject meta data like languages to CallContexts.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface CallMeta
{
    /**
     * Returns a list of the languages the calling client is willing to accept.
     * @return the list.
     */
    PreferredLocale [] getLanguages();

    /**
     * Intersects the list of preferred client languages with a server
     * given list and returns the one which is best fitting.
     * @param locales The list of languages the server provides.
     * @return The best fitting language.
     */
    Locale getPreferredLocale(Locale [] locales);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
