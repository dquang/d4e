/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase.rest;

import org.dive4elements.artifactdatabase.DefaultCallMeta;
import org.dive4elements.artifactdatabase.DefaultPreferredLocale;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.PreferredLocale;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.Preference;

import org.restlet.representation.Representation;

import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Base class for the resources of REST interface of the artifact database.
 * Primarily used to unify the logging.
 * @author <a href="mailto:sascha.teichmann@intevation">Sascha L. Teichmann</a>
 */
public class BaseResource
extends      ServerResource
{
    private static Logger logger = LogManager.getLogger(BaseResource.class);

    /**
     * Default constructor.
     */
    public BaseResource() {
    }

    /**
     * Overrides the post method of ServerResource to handle some
     * exceptions and to the required logging.
     * The call bridges to #innerPost(Representation) which
     * should be overwitten by the subclasses to do the real
     * request processing.
     * @param requestRepr The incoming represention of the HTTP request.
     * @return The representation produced by #innerPost(Representation).
     * @throws ResourceException Thrown if something went wrong during
     * request processing.
     */
    @Override
    protected Representation post(Representation requestRepr)
    throws    ResourceException
    {
        try {
            return innerPost(requestRepr);
        }
        catch (ResourceException re) {
            throw re;
        }
        catch (RuntimeException re) {
            logger.error(re.getLocalizedMessage(), re);
            throw re;
        }
    }

    /**
     * Trivial implementation of innerPost() which is called by
     * #post(Representation) which simply calls super.post(Representation).
     * This should be overwritten by subclasses which need POST support.
     * @param requestRepr The incoming representation of the request.
     * @return The representation produced by super.post(Representation).
     * @throws ResourceException Thrown if something went wrong during
     * request processing.
     */
    protected Representation innerPost(Representation requestRepr)
    throws    ResourceException
    {
        return super.post(requestRepr);
    }

    /**
     * Wrapper around get() of the super class to handle some exceptions
     * and do the corresponing logging. The call is bridged to #innerGet()
     * which should be overwritten by subclasses.
     * @return The representation produced by #innerGet()
     * @throws ResourceException Thrown if something went wrong during
     * request processing.
     */
    @Override
    protected Representation get()
    throws    ResourceException
    {
        try {
            return innerGet();
        }
        catch (ResourceException re) {
            throw re;
        }
        catch (RuntimeException re) {
            logger.error(re.getLocalizedMessage(), re);
            throw re;
        }
    }

    /**
     * Trivial implementaion of innerGet() which simply calls
     * super.get() to produce some output representation. This method
     * should be overwritten by subclasses which need GET support.
     * @return The representation produced by super.get().
     * @throws ResourceException Thrown if something went wrong during
     * request processing.
     */
    protected Representation innerGet()
    throws    ResourceException
    {
        return super.get();
    }

    /**
     * Returns meta information (preferred languages et. al.)
     * of the current HTTP request.
     * @return the meta information
     */
    protected CallMeta getCallMeta() {
        ClientInfo clientInfo = getClientInfo();

        List<Preference<Language>> pl = clientInfo.getAcceptedLanguages();

        PreferredLocale [] languages = new PreferredLocale[pl.size()];

        int index = 0;

        for (Preference<Language> p: pl) {
            String lang    = p.getMetadata().getName();
            float  quality = p.getQuality();
            languages[index++] = new DefaultPreferredLocale(lang, quality);
        }

        return new DefaultCallMeta(languages);
    }


    /**
     * Returns the artifact database stored in the context of the REST
     * application.
     *
     * @return the artifact database.
     */
    protected ArtifactDatabase getArtifactDatabase() {
        return (ArtifactDatabase) getContext().getAttributes().get("database");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
