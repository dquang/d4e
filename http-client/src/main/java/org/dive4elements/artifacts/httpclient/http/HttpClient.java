/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.http;

import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.response.ResponseHandler;
import org.dive4elements.artifacts.httpclient.objects.Artifact;
import org.dive4elements.artifacts.httpclient.objects.ArtifactFactory;
import org.dive4elements.artifacts.httpclient.utils.ArtifactCreator;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface HttpClient {

    ArtifactFactory[] getArtifactFactories()
    throws ConnectionException;

    /*******************************
     * Artifact API
     *******************************/

    Object create(Document createDocument, ArtifactCreator creator)
    throws ConnectionException;

    Object describe(
        Artifact        artifact,
        Document        describeDocument,
        ResponseHandler handler)
    throws ConnectionException;

    Object feed(Artifact artifact, Document feedDocument, ResponseHandler handler)
    throws ConnectionException;

    Object advance(
        Artifact        artifact,
        Document        advanceDocument,
        ResponseHandler handler)
    throws ConnectionException;

    void out(
        Artifact     artifact,
        Document     outDocument,
        String       target,
        OutputStream out)
    throws ConnectionException;


    /*******************************
     * Service API
     *******************************/

     Document callService(String url, String service, Document input)
     throws ConnectionException;

     Object callService(
         String          url,
         String          service,
         Document        input,
         ResponseHandler handler)
     throws ConnectionException;


    /*******************************
     * Collections API
     *******************************/

    Object createCollection(
        Document        createDocument,
        String          ownerId,
        ResponseHandler handler)
    throws ConnectionException;

    Object doCollectionAction(
        Document        actionDocument,
        String          uuid,
        ResponseHandler handler)
    throws ConnectionException;

    void collectionOut(
        Document     outDocument,
        String       uuid,
        String       type,
        OutputStream out)
    throws ConnectionException;

    InputStream collectionOut(
        Document    doc,
        String      uuid,
        String      type)
    throws ConnectionException;


    /*******************************
     * Users API
     *******************************/

    Document listUsers()
    throws ConnectionException;

    Document listUserCollections(String userid)
    throws ConnectionException;

    Document createUser(Document doc)
    throws ConnectionException;

    Document findUser(Document doc)
    throws ConnectionException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
