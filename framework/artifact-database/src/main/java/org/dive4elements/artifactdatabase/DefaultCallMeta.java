/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.PreferredLocale;

import java.util.Locale;

/**
 * Default implementation of CallMeta. It provides a list of
 * preferred langauages and implements an intersection mechanism
 * to figure out the best matching language given a list of server
 * provided languages.
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultCallMeta
implements   CallMeta
{
    /**
     * The list of preferred languages.
     */
    protected PreferredLocale [] languages;

    /**
     * Default constructor.
     */
    public DefaultCallMeta() {
    }

    /**
     * Creates new DefaultCallMeta with a given list of languages.
     * @param languages The list of preferred languages.
     */
    public DefaultCallMeta(PreferredLocale [] languages) {
        this.languages = languages;
    }

    public PreferredLocale [] getLanguages() {
        return languages;
    }

    public Locale getPreferredLocale(Locale [] locales) {
        if (locales == null || locales.length == 0) {
            return null;
        }

        Locale best    = null;
        float  quality = -Float.MAX_VALUE;

        for (int i = 0; i < locales.length; ++i) {
            Locale wish         = locales[i];
            String wishLanguage = wish.getLanguage();

            for (int j = 0; j < languages.length; ++j) {
                PreferredLocale have        = languages[j];
                Locale          haveLocale  = have.getLocale();
                if (haveLocale.getLanguage().equals(wishLanguage)) {
                    float haveQuality = have.getQuality();
                    if (haveQuality > quality) {
                        quality = haveQuality;
                        best    = wish;
                    }
                    break; // Languages should not contain
                           // same locale twice.
                }
            }
        }

        return best == null
            ? locales[0]
            : best;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
