/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.http;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;
import org.dive4elements.artifacts.httpclient.http.response.ResponseHandler;
import org.dive4elements.artifacts.httpclient.http.response.StreamResponseHandler;
import org.dive4elements.artifacts.httpclient.objects.Artifact;
import org.dive4elements.artifacts.httpclient.objects.ArtifactFactory;
import org.dive4elements.artifacts.httpclient.utils.ArtifactProtocolUtils;
import org.dive4elements.artifacts.httpclient.utils.ArtifactCreator;

/**
 * Client to artifact-server.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class HttpClientImpl implements HttpClient {

    private static final Logger logger = LogManager.getLogger(HttpClient.class);

    /** The URL part of the resource to list the existing users of the server.*/
    public static final String PATH_LIST_USERS = "/list-users";

    /** The URL part of the resource to list the Collections owned by a specific
     * user.*/
    public static final String PATH_USER_COLLECTIONS = "/list-collections";

    /** The URL part og the resource to create a new user on the server.*/
    public static final String PATH_CREATE_USER = "/create-user";

    /** The URL part og the resource to find an existing user on the server.*/
    public static final String PATH_FIND_USER = "/find-user";

    /** The URL part of the resource to call a specific service.*/
    public static final String PATH_SERVICE = "/service";

    /** The URL path of the resource to create new artifact collections.*/
    public static final String PATH_CREATE_COLLECTION = "/create-collection";

    /** The URL path of the resource to work with an artifact collections.*/
    public static final String PATH_ACTION_COLLECTION = "/collection";

    /** The URL path of the resource to work with an artifact collections.*/
    public static final String PATH_OUT_COLLECTION = "/collection";

    private String serverUrl;

    private String localeString;


    private static final ThreadLocal<Client> CLIENT =
        new ThreadLocal<Client>() {
            @Override
            protected Client initialValue() {
                logger.debug("create new HTTP client");
                return new Client(Protocol.HTTP);
            }
         };

    public HttpClientImpl(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    /**
     * This constructor might be used to modify the request's locale manually.
     * E.g. the localization should not be based on the configured browser
     * locale, but site specific configuration - than you are able to set the
     * locale in this constructor.
     *
     * @param serverUrl The url that is used for the request.
     * @param localeString The string representation of the desired locale.
     */
    public HttpClientImpl(String serverUrl, String localeString) {
        this(serverUrl);

        this.localeString = localeString;
    }


    @Override
    public ArtifactFactory[] getArtifactFactories()
    throws ConnectionException
    {
        ResponseHandler handler = new DocumentResponseHandler();

        try {
            String    url   = serverUrl + "/factories";
            Document result = (Document) handler.handle(doGet(url));

            return ArtifactProtocolUtils.extractArtifactFactories(result);
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed. No Factories recieved.");
        }
    }


    /**
     * This method creates a new artifact in the artifact server and returns
     * this artifact. The new artifact is created using <i>creator</i>. If no
     * {@link ArtifactCreator} is given (null), an {@link Artifact} is returned.
     *
     * @param doc The CREATE document.
     * @param creator The {@link ArtifactCreator} that is used to extract the
     * new artifact from response document of the server.
     *
     * @return the new artifact.
     */
    @Override
    public Object create(Document doc, ArtifactCreator creator)
    throws ConnectionException
    {
        ResponseHandler handler = new DocumentResponseHandler();

        try {
            String   url    = serverUrl + "/create";
            Document result = (Document) handler.handle(doPost(url, doc));

            return creator == null
                ? ArtifactProtocolUtils.extractArtifact(result)
                : creator.create(result);
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed. No Artifact created.");
        }
    }


    @Override
    public Object describe(
        Artifact        artifact,
        Document        doc,
        ResponseHandler handler)
    throws ConnectionException
    {
        try {
            String   url    = serverUrl + "/artifact/" + artifact.getUuid();
            return handler.handle(doPost(url, doc));
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed: " + ioe.getMessage());
        }
    }


    @Override
    public Object feed(Artifact artifact, Document doc, ResponseHandler handler)
    throws ConnectionException
    {
        try {
            String   url    = serverUrl + "/artifact/" + artifact.getUuid();
            Document result = (Document) handler.handle(doPost(url, doc));

            return result;
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed: " + ioe.getMessage());
        }
    }


    @Override
    public Object advance(Artifact artifact, Document doc, ResponseHandler handler)
    throws ConnectionException
    {
        try {
            String   url    = serverUrl + "/artifact/" + artifact.getUuid();
            Document result = (Document) handler.handle(doPost(url, doc));

            return result;
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed: " + ioe.getMessage());
        }
    }


    @Override
    public void out(
        Artifact     artifact,
        Document     doc,
        String       target,
        OutputStream out)
    throws ConnectionException
    {
        try {
            String url =
                serverUrl
                + "/artifact/"
                + artifact.getUuid()
                + "/" + target;

            ResponseHandler handler = new StreamResponseHandler();

            InputStream stream = (InputStream) handler.handle(doPost(url, doc));
            try {
                byte[] b = new byte[4096];
                int i;
                while ((i = stream.read(b)) >= 0) {
                    out.write(b, 0, i);
                }
            }
            finally {
                stream.close();
            }
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed: " + ioe.getMessage());
        }
    }


    //==============================
    // HTTP specific methods
    //==============================

    private Response doPost(String url, Document body) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Start HTTP-POST request to: " + url);
        }

        Client client   = getClient();
        Request request = prepareRequest(Method.POST, url);

        Representation representation = new DomRepresentation(
            MediaType.APPLICATION_XML,
            body);

        request.setEntity(representation);
        Response response = client.handle(request);

        logger.debug("RESPONSE: " + response);

        Status status = response.getStatus();
        if (status.getCode() != 200) {
            logger.error("Response status: " + status.getCode());
            throw new IOException(status.getDescription());
        }

        return response;
    }


    private static Client getClient() {
        return CLIENT.get();
    }


    private Response doGet(String url) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Start HTTP-POST request to: "+ url);
        }

        Client client   = getClient();
        Request request = prepareRequest(Method.GET, url);

        Response response = client.handle(request);

        Status status = response.getStatus();
        if (status.getCode() != 200) {
            logger.error("Response status: " + status.getCode());
            throw new IOException(status.getDescription());
        }

        return response;
    }


    /**
     * This method prepares the request object.
     *
     * @param method The HTTP method (GET,POST).
     * @param url The URL used for the request.
     *
     * @return the request object.
     */
    private Request prepareRequest(Method method, String url) {
        Request request = new Request(method, url);

        ClientInfo info = request.getClientInfo();

        setLocale(info);

        request.setClientInfo(info);

        return request;
    }


    /**
     * This method is called to set the request's locale.
     *
     * @param info The ClientInfo that is used to provide request information.
     */
    private void setLocale(ClientInfo info) {
        if (localeString == null) {
            return;
        }

        List<Preference<Language>> accepted =
            new ArrayList<Preference<Language>>();

        Language lang = Language.valueOf(localeString);

        if (lang != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Set locale of the request object: " + lang.toString());
            }

            Preference<Language> pref = new Preference<Language>();
            pref.setMetadata(lang);
            accepted.add(pref);

            info.setAcceptedLanguages(accepted);
        }
    }


    //==============================
    // Collection API
    //==============================

    /**
     * This method triggers the artifact servers resource to create a new
     * artifact collection.
     *
     * @param create The CREATE document for the collection.
     * @param ownerId The uuid of the creator.
     * @param handler The handler that is used to create the result object.
     *
     * @return a result object created by <i>handler</i>.
     */
    public Object createCollection(
        Document        create,
        String          ownerId,
        ResponseHandler handler)
    throws ConnectionException
    {
        String url = serverUrl + PATH_CREATE_COLLECTION + "/" + ownerId;

        try {
            return handler.handle(doPost(url, create));
        }
        catch (IOException ioe) {
            throw new ConnectionException(ioe.getMessage(), ioe);
        }
    }


    /**
     * This method might be used to trigger a collection specific action. The
     * action that is executed depends on the document <i>actionDoc</i>.
     *
     * @param actionDoc The document that describes the action to be executed.
     * @param uuid      The uuid of the collection.
     * @param handler   The handler that is used to create the result object.
     *
     * @return a result object created by <i>handler</i>.
     */
    public Object doCollectionAction(
        Document        actionDoc,
        String          uuid,
        ResponseHandler handler)
    throws ConnectionException
    {
        String url = serverUrl + PATH_ACTION_COLLECTION + "/" + uuid;

        try {
            return handler.handle(doPost(url, actionDoc));
        }
        catch (IOException ioe) {
            throw new ConnectionException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Write out() operation of a Collection to <i>out</i>.
     *
     * @param doc The request document for the out() operation.
     * @param uuid The identifier of the Collection.
     * @param type The name of the output type.
     * @param out The OutputStream.
     */
    public void collectionOut(
        Document     doc,
        String       uuid,
        String       type,
        OutputStream out)
    throws ConnectionException
    {
        try {
            InputStream stream = collectionOut(doc, uuid, type);

            try {
                byte[] b = new byte[4096];
                int i;
                while ((i = stream.read(b)) >= 0) {
                    out.write(b, 0, i);
                }
            }
            finally {
                stream.close();
            }
        }
        catch (IOException ioe) {
            throw new ConnectionException(ioe.getMessage(), ioe);
        }
    }


    /**
     * This method triggers the out() operation of a Collection. The result of
     * this operation is returned as an InputStream.
     *
     * @param doc The request document for the out() operation.
     * @param uuid The identifier of the Collection.
     * @param type The name of the output type.
     *
     * @return an InputStream.
     */
    public InputStream collectionOut(
        Document    doc,
        String      uuid,
        String      type)
    throws ConnectionException
    {
        String url = serverUrl + PATH_OUT_COLLECTION + "/" + uuid + "/" + type;

        ResponseHandler handler = new StreamResponseHandler();

        try {
            return (InputStream) handler.handle(doPost(url, doc));
        }
        catch (IOException ioe) {
            throw new ConnectionException(ioe.getMessage(), ioe);
        }
    }


    /*******************************
     * Service API
     *******************************/

     public Document callService(String url, String service, Document input)
     throws ConnectionException
     {
         DocumentResponseHandler handler = new DocumentResponseHandler();

         return (Document) callService(url, service, input, handler);
     }


     public Object callService(
         String          url,
         String          service,
         Document        input,
         ResponseHandler handler)
     throws ConnectionException {
        if (logger.isDebugEnabled()) {
            logger.debug("Start service call to '" + service + "'");
        }

        try {
            String serverUrl = url + PATH_SERVICE + "/" + service;
            return handler.handle(doPost(serverUrl, input));
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed: " + ioe.getMessage());
        }
    }


    /*******************************
     * Users API
     *******************************/

    public Document listUsers()
    throws ConnectionException
    {
        ResponseHandler handler = new DocumentResponseHandler();
        String    url           = serverUrl + PATH_LIST_USERS;

        try {
            return (Document) handler.handle(doGet(url));
        }
        catch (IOException ioe) {
            throw new ConnectionException(ioe.getMessage(), ioe);
        }
    }


    public Document listUserCollections(String userid)
    throws ConnectionException
    {
        ResponseHandler handler = new DocumentResponseHandler();

        String url = serverUrl + PATH_USER_COLLECTIONS + "/" + userid;

        try {
            return (Document) handler.handle(doGet(url));
        }
        catch (IOException ioe) {
            throw new ConnectionException(ioe.getMessage(), ioe);
        }
    }

    @Override
    public Document createUser(Document doc)
    throws ConnectionException {
        ResponseHandler handler = new DocumentResponseHandler();

        String url = this.serverUrl + PATH_CREATE_USER;

        try {
            return (Document) handler.handle(doPost(url, doc));
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed: " + ioe.getMessage());
        }
    }

    @Override
    public Document findUser(Document doc)
    throws ConnectionException {
        ResponseHandler handler = new DocumentResponseHandler();

        String url = this.serverUrl + PATH_FIND_USER;

        try {
            return (Document) handler.handle(doPost(url, doc));
        }
        catch (IOException ioe) {
            throw new ConnectionException(
                "Connection to server failed: " + ioe.getMessage());
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
