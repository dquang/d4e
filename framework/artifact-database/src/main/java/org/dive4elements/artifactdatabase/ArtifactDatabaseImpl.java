/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.artifactdatabase.Backend.PersistentArtifact;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.ArtifactCollectionFactory;
import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.ArtifactSerializer;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.CollectionItem;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.Hook;
import org.dive4elements.artifacts.Message;
import org.dive4elements.artifacts.Service;
import org.dive4elements.artifacts.ServiceFactory;
import org.dive4elements.artifacts.User;
import org.dive4elements.artifacts.UserFactory;

import java.io.IOException;
import java.io.OutputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathConstants;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The core implementation of artifact database. This layer exposes
 * the needed methods to the artifact runtime system which e.g. may
 * expose them via REST. The concrete persistent representation of the
 * artifacts is handled by the {@link Backend backend}.
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class ArtifactDatabaseImpl
implements   ArtifactDatabase,
             DatabaseCleaner.LockedIdsProvider,
             Backend.FactoryLookup
{
    private static Logger logger =
        LogManager.getLogger(ArtifactDatabaseImpl.class);

    /** The key under which the artifact database is stored in the global
     * context.*/
    public static final String GLOBAL_CONTEXT_KEY = "global.artifact.database";

    /** Message that is returned if an operation was successful.*/
    public static final String OPERATION_SUCCESSFUL =
        "SUCCESS";

    /** Message that is returned if an operation failed.*/
    public static final String OPERATION_FAILURE =
        "FAILURE";

    /**
     * Error message issued if a requested artifact factory
     * is not registered to this database.
     */
    public static final String NO_SUCH_FACTORY =
        "No such factory";

    /**
     * Error message issued if a requested artifact is not found
     * in this database.
     */
    public static final String NO_SUCH_ARTIFACT =
        "No such artifact";

    /**
     * Error message issued if a requested artifact is not found
     * in this database.
     */
    public static final String NO_SUCH_COLLECTION =
        "No such collection";

    /**
     * Error message issued if the creation of an artifact failed.
     */
    public static final String CREATION_FAILED =
        "Creation of artifact failed";

    /**
     * Error message if an severe internal error occurred.
     */
    public static final String INTERNAL_ERROR =
        "Creation of artifact failed";

    /**
     * Error message issued if a requested service is not
     * offered by this database.
     */
    public static final String NO_SUCH_SERVICE =
        "No such service";

    /**
     * Default digest hash to be used while im-/exporting artifacts.
     */
    public static final String DIGEST_ALGORITHM =
        "SHA-1";

    /**
     * XPath to get the checksum from an XML representation of
     * an exported artifact.
     */
    public static final String XPATH_IMPORT_CHECKSUM =
        "/art:action/art:data/@checksum";

    /**
     * XPath to get the name of the factory which should be
     * used to revive an antrifact that is going to be imported.
     */
    public static final String XPATH_IMPORT_FACTORY =
        "/art:action/art:data/@factory";

    /**
     * XPath to get the base64 encoded data of an artifact
     * that is going to be imported.
     */
    public static final String XPATH_IMPORT_DATA =
        "/art:action/art:data/text()";

    /**
     * Error message issued if the checksum of an
     * artifact to be imported has an invalid syntax.
     */
    public static final String INVALID_CHECKSUM =
        "Invalid checksum";

    /**
     * Error message issued the checksum validation
     * of an artifact to be imported fails.
     */
    public static final String CHECKSUM_MISMATCH =
        "Mismatching checksum";

    /**
     * Error message issued if an artifact to be imported
     * does not have any data.
     */
    public static final String NO_DATA =
        "No data";

    /**
     * Error message issued if the deserialization of
     * an artifact to be imported fails.
     */
    public static final String INVALID_ARTIFACT =
        "Invalid artifact";


    // User constants

    /**
     * Error message issued if the creation of a user failed.
     */
    public static final String USER_CREATION_FAILED =
        "Creation of user failed.";

    /** XPath to figure out the name of a new user.*/
    public static final String XPATH_USERNAME =
        "/art:action/art:user/@name";

    /** XPath to figure out the role of a new user.*/
    public static final String XPATH_USERROLE =
        "/art:action/art:user/art:role";

    /** XPath to figure out the account of a new user.*/
    public static final String XPATH_USERACCOUNT =
        "/art:action/art:user/art:account/@name";

    /** XPath to figure out the account of when searching for a user .*/
    public static final String XPATH_USERACCOUNT_FIND =
        "/art:action/art:account/@name";

    /** Error message if a specified user does not exist.*/
    public static final String NO_SUCH_USER =
        "No such user";

    /** Error message if no username is given for user creation.*/
    public static final String NO_USERNAME =
        "Invalid username";

    /** Error message if no user account is given for user creation.*/
    public static final String NO_USERACCOUNT =
        "Invalid user account name";

    // Collection constants

    /**
     * Error message issued if the creation of a collection failed.
     */
    public static final String COLLECTION_CREATION_FAILED =
        "Creation of collection failed";

    /**
     * XPath to figure out the name of a collection described in the incoming
     * document.
     */
    public static final String XPATH_COLLECTION_NAME =
        "/art:action/art:type/art:collection/@name";

    /**
     * XPath to figure out the attributes for a collection.
     */
    public static final String XPATH_COLLECTION_ATTRIBUTE =
        "/art:action/art:type/art:collection/art:attribute";

    /**
     * XPath to figure out the attributes for an artifact that is put into a
     * collection.
     */
    public static final String XPATH_COLLECTION_ITEM_ATTRIBUTE =
        "/art:action/art:type/art:artifact/art:attribute";

    /**
     * XPath to figure out the time to live value for setting a new TTL.
     */
    public static final String XPATH_COLLECTION_TTL =
        "/art:action/art:type/art:ttl/@value";


    /**
     * This inner class allows the deferral of writing the output
     * of the artifact's out() call.
     */
    public class DeferredOutputImpl
    implements   DeferredOutput
    {
        /**
         * The persistence wrapper around a living artifact.
         */
        protected PersistentArtifact artifact;
        /**
         * The output type.
         */
        protected String type;
        /**
         * The input document for the artifact's out() call.
         */
        protected Document           format;
        /**
         * The meta information of the artifact's out() call.
         */
        protected CallMeta           callMeta;

        /**
         * Default constructor.
         */
        public DeferredOutputImpl() {
        }

        /**
         * Constructor to create a deferred execution unit for
         * the artifact's out() call given an artifact, an input document
         * an the meta information.
         * @param artifact The persistence wrapper around a living artifact.
         * @param format   The input document for the artifact's out() call.
         * @param callMeta The meta information of the artifact's out() call.
         */
        public DeferredOutputImpl(
            PersistentArtifact artifact,
            String             type,
            Document           format,
            CallMeta           callMeta
        ) {
            this.artifact = artifact;
            this.type     = type;
            this.format   = format;
            this.callMeta = callMeta;
        }

        public void write(OutputStream output) throws IOException {

            ArtifactCallContext cc = new ArtifactCallContext(
                ArtifactDatabaseImpl.this,
                CallContext.TOUCH,
                callMeta,
                artifact);

            try {
                artifact.getArtifact().out(type, format, output, cc);
            }
            finally {
                cc.postCall();
            }
        }
    } // class DeferredOutputImpl


    /**
     * This inner class allows the deferral of writing the output
     * of the artifact's out() call.
     */
    public class DeferredCollectionOutputImpl
    implements   DeferredOutput
    {
        /**
         * The persistence wrapper around a living collection.
         */
        protected ArtifactCollection collection;
        /**
         * The output type.
         */
        protected String type;
        /**
         * The input document for the collection's out() call.
         */
        protected Document format;
        /**
         * The meta information of the collection's out() call.
         */
        protected CallMeta callMeta;

        /**
         * Default constructor.
         */
        public DeferredCollectionOutputImpl() {
        }

        /**
         * Constructor to create a deferred execution unit for
         * the collection's out() call given a collection, an input document
         * an the meta information.
         * @param collection The collection.
         * @param format   The input document for the collection's out() call.
         * @param callMeta The meta information of the collection's out() call.
         */
        public DeferredCollectionOutputImpl(
            ArtifactCollection collection,
            String             type,
            Document           format,
            CallMeta           callMeta
        ) {
            this.collection = collection;
            this.type       = type;
            this.format     = format;
            this.callMeta   = callMeta;
        }

        public void write(OutputStream output) throws IOException {

            CollectionCallContext cc = new CollectionCallContext(
                ArtifactDatabaseImpl.this,
                CallContext.TOUCH,
                callMeta,
                collection);

            try {
                collection.out(type, format, output, cc);
            }
            finally {
                cc.postCall();
            }
        }
    } // class DeferredCollectionOutputImpl

    /**
     * List of name/description pairs needed for
     * {@link #artifactFactoryNamesAndDescriptions() }.
     */
    protected String [][] factoryNamesAndDescription;
    /**
     * Map to access artifact factories by there name.
     */
    protected HashMap     name2factory;

    /**
     * List of name/description pairs needed for
     * {@link #serviceNamesAndDescriptions() }.
     */
    protected String [][] serviceNamesAndDescription;
    /**
     * Map to access services by there name.
     */
    protected HashMap     name2service;

    /**
     * The factory that is used to create new artifact collections.
     */
    protected ArtifactCollectionFactory collectionFactory;

    /**
     * The factory that is used to create and list users.
     */
    protected UserFactory userFactory;

    /**
     * Reference to the storage backend.
     */
    protected Backend     backend;
    /**
     * Reference of the global context of the artifact runtime system.
     */
    protected GlobalContext context;

    /**
     * The signing secret to be used for ex-/importing artifacts.
     */
    protected byte []     exportSecret;

    /**
     * A set of ids of artifact which currently running in background.
     * This artifacts should not be removed from the database by the
     * database cleaner.
     */
    protected HashSet<Integer> backgroundIds;

    /**
     * A list of background messages for Artifacts and Collections.
     */
    protected Map<String, LinkedList<Message>> backgroundMsgs;


    protected CallContext.Listener callContextListener;

    /**
     * Hooks that are executed after an artifact has been fed.
     */
    protected List<Hook> postFeedHooks;

    /**
     * Hooks that are executed after an artifact has advanced.
     */
    protected List<Hook> postAdvanceHooks;

    /**
     * Hooks that are executed after an artifact's describe() operation was
     * called.
     */
    protected List<Hook> postDescribeHooks;

    protected List<LifetimeListener> lifetimeListeners;

    /**
     * Default constructor.
     */
    public ArtifactDatabaseImpl() {
    }

    /**
     * Constructor to create a artifact database with the given
     * bootstrap parameters like artifact- and service factories et. al.
     * Created this way the artifact database has no backend.
     * @param bootstrap The parameters to start this artifact database.
     */
    public ArtifactDatabaseImpl(FactoryBootstrap bootstrap) {
        this(bootstrap, null);
    }

    /**
     * Constructor to create a artifact database with the a given
     * backend and
     * bootstrap parameters like artifact- and service factories et. al.
     * @param bootstrap The parameters to start this artifact database.
     * @param backend   The storage backend.
     */
    public ArtifactDatabaseImpl(FactoryBootstrap bootstrap, Backend backend) {

        logger.debug("new ArtifactDatabaseImpl");

        backgroundIds  = new HashSet<Integer>();
        backgroundMsgs = new HashMap<String, LinkedList<Message>>();

        setupArtifactCollectionFactory(bootstrap);
        setupArtifactFactories(bootstrap);
        setupServices(bootstrap);
        setupUserFactory(bootstrap);
        setupCallContextListener(bootstrap);
        setupHooks(bootstrap);
        setupLifetimeListeners(bootstrap);

        context = bootstrap.getContext();
        context.put(GLOBAL_CONTEXT_KEY, this);

        exportSecret = bootstrap.getExportSecret();

        wireWithBackend(backend, bootstrap);
    }

    public CallContext.Listener getCallContextListener() {
        return callContextListener;
    }

    public void setCallContextListener(
        CallContext.Listener callContextListener
    ) {
        this.callContextListener = callContextListener;
    }


    public void setPostFeedHook(List<Hook> postFeedHooks) {
        this.postFeedHooks = postFeedHooks;
    }

    public void setPostAdvanceHook(List<Hook> postAdvanceHooks) {
        this.postAdvanceHooks = postAdvanceHooks;
    }

    public void setPostDescribeHook(List<Hook> postDescribeHooks) {
        this.postDescribeHooks = postDescribeHooks;
    }

    /**
     * Used to extract the artifact collection factory from bootstrap.
     *
     * @param bootstrap The bootstrap parameters.
     */
    protected void setupArtifactCollectionFactory(FactoryBootstrap bootstrap) {
        collectionFactory = bootstrap.getArtifactCollectionFactory();
    }

    /**
     * Used to extract the artifact factories from the bootstrap
     * parameters and building the internal lookup tables.
     * @param bootstrap The bootstrap parameters.
     */
    protected void setupArtifactFactories(FactoryBootstrap bootstrap) {
        name2factory  = new HashMap();

        ArtifactFactory [] factories = bootstrap.getArtifactFactories();
        factoryNamesAndDescription = new String[factories.length][];

        for (int i = 0; i < factories.length; ++i) {

            ArtifactFactory factory = factories[i];

            String name        = factory.getName();
            String description = factory.getDescription();

            factoryNamesAndDescription[i] =
                new String [] { name, description };

            name2factory.put(name, factory);
        }
    }

    /**
     * Used to extract the callContextListener from the bootstrap.
     *
     * @param bootstrap The bootstrap parameters.
     */
    protected void setupCallContextListener(FactoryBootstrap bootstrap) {
        setCallContextListener(bootstrap.getCallContextListener());
    }


    protected void setupHooks(FactoryBootstrap bootstrap) {
        setPostFeedHook(bootstrap.getPostFeedHooks());
        setPostAdvanceHook(bootstrap.getPostAdvanceHooks());
        setPostDescribeHook(bootstrap.getPostDescribeHooks());
    }

    protected void setupBackendListeners(FactoryBootstrap bootstrap) {
        logger.debug("setupBackendListeners");
        List<BackendListener> bls = bootstrap.getBackendListeners();
        if (bls != null && !bls.isEmpty()) {
            for (BackendListener listener: bls) {
                listener.setup(context);
            }
            backend.addAllListeners(bls);
        }
    }

    protected void setupLifetimeListeners(FactoryBootstrap bootstrap) {
        this.lifetimeListeners = bootstrap.getLifetimeListeners();
    }

    /**
     * Used to extract the user factory from the bootstrap.
     */
    protected void setupUserFactory(FactoryBootstrap bootstrap) {
        userFactory = bootstrap.getUserFactory();
    }

    /**
     * Used to extract the service factories from the bootstrap
     * parameters, setting up the services and building the internal
     * lookup tables.
     * @param bootstrap The bootstrap parameters.
     */
    protected void setupServices(FactoryBootstrap bootstrap) {

        name2service  = new HashMap();

        ServiceFactory [] serviceFactories =
            bootstrap.getServiceFactories();

        serviceNamesAndDescription =
            new String[serviceFactories.length][];

        for (int i = 0; i < serviceFactories.length; ++i) {
            ServiceFactory factory = serviceFactories[i];

            String name        = factory.getName();
            String description = factory.getDescription();

            serviceNamesAndDescription[i] =
                new String [] { name, description };

            name2service.put(
                name,
                factory.createService(bootstrap.getContext()));
        }

    }

    /**
     * Wires a storage backend to this artifact database and
     * establishes a callback to be able to revive artifacts
     * via the serializers of this artifact factories.
     * @param backend The backend to be wired with this artifact database.
     */
    public void wireWithBackend(Backend backend, FactoryBootstrap bootstrap) {
        logger.debug("wireWithBackend");
        if (backend != null) {
            this.backend = backend;
            backend.setFactoryLookup(this);
            setupBackendListeners(bootstrap);
        }
    }

    /**
     * Called after an backgrounded artifact signals its
     * will to be written back to the backend.
     * @param artifact The persistence wrapper around
     * the backgrounded artifact.
     * @param action The action to be performed.
     */
    protected void fromBackground(PersistentArtifact artifact, int action) {
        logger.warn("BACKGROUND processing is not fully implemented, yet!");
        switch (action) {
            case CallContext.NOTHING:
                break;
            case CallContext.TOUCH:
                artifact.touch();
                break;
            case CallContext.STORE:
                artifact.store();
                break;
            default:
                logger.warn("operation not allowed in fromBackground");
        }
        removeIdFromBackground(artifact.getId());
        removeBackgroundMessages(artifact.getArtifact().identifier());
    }

    /**
     * Removes an artifact's database id from the set of backgrounded
     * artifacts. The database cleaner is now able to remove it safely
     * from the database again.
     * @param id The database id of the artifact.
     */
    protected void removeIdFromBackground(int id) {
        synchronized (backgroundIds) {
            backgroundIds.remove(id);
        }
    }


    /**
     * Removes all messages that have been added to the <i>backgroundMsgs</i>
     * list.
     *
     * @param uuid The UUID of an artifact or collection.
     */
    protected void removeBackgroundMessages(String uuid) {
        logger.debug("Remove background messages for: " + uuid);

        synchronized (backgroundMsgs) {
            backgroundMsgs.remove(uuid);
        }
    }

    /**
     * Adds an artifact's database id to the set of artifacts
     * running in backgroound. To be in this set prevents the
     * artifact to be removed from the database by the database cleaner.
     * @param id The database id of the artifact to be protected
     * from being removed from the database.
     */
    protected void addIdToBackground(int id) {
        synchronized (backgroundIds) {
            backgroundIds.add(Integer.valueOf(id));
        }
    }

    /**
     * Adds a <i>Message</i> to the background messages list of the Artifact or
     * Collection.
     *
     * @param uuid The UUID of the Artifact or Collection.
     * @param msg The message that should be added to the background messages
     * list.
     */
    public void addBackgroundMessage(String uuid, Message msg) {
        logger.debug("Add new background messsage for: " + uuid);

        synchronized (backgroundMsgs) {
            LinkedList<Message> messages = backgroundMsgs.get(uuid);

            if (messages == null) {
                messages = new LinkedList<Message>();
                backgroundMsgs.put(uuid, messages);
            }

            messages.addLast(msg);
        }
    }

    public Set<Integer> getLockedIds() {
        synchronized (backgroundIds) {
            return new HashSet<Integer>(backgroundIds);
        }
    }

    /**
     * Returns the background <i>Message</i>s for a specific Artifact or
     * Collection.
     *
     * @param uuid The Artifact's or Collection's UUID.
     *
     * @return a <i>List</i> of <i>Message</i>s or null if no messages are
     * existing.
     */
    public LinkedList<Message> getBackgroundMessages(String uuid) {
        logger.debug("Retrieve background message for: " + uuid);

        synchronized (backgroundMsgs) {
            return backgroundMsgs.get(uuid);
        }
    }

    public String [][] artifactFactoryNamesAndDescriptions() {
        return factoryNamesAndDescription;
    }

    public ArtifactFactory getInternalArtifactFactory(String factoryName) {
        return getArtifactFactory(factoryName);
    }

    public ArtifactFactory getArtifactFactory(String factoryName) {
        return (ArtifactFactory)name2factory.get(factoryName);
    }

    public UserFactory getUserFactory() {
        return userFactory;
    }

    public ArtifactCollectionFactory getArtifactCollectionFactory() {
        return collectionFactory;
    }

    public Document createArtifactWithFactory(
        String   factoryName,
        CallMeta callMeta,
        Document data
    )
    throws ArtifactDatabaseException
    {
        logger.debug("ArtifactDatabaseImpl.createArtifactWithFactory "
             + factoryName);
        ArtifactFactory factory = getArtifactFactory(factoryName);

        if (factory == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        Artifact artifact = factory.createArtifact(
            backend.newIdentifier(),
            context,
            callMeta,
            data);

        if (artifact == null) {
            throw new ArtifactDatabaseException(CREATION_FAILED);
        }

        PersistentArtifact persistentArtifact;

        try {
            persistentArtifact = backend.storeInitially(
                artifact,
                factory,
                factory.timeToLiveUntouched(artifact, context));
        }
        catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new ArtifactDatabaseException(CREATION_FAILED);
        }

        ArtifactCallContext cc = new ArtifactCallContext(
            ArtifactDatabaseImpl.this,
            CallContext.NOTHING,
            callMeta,
            persistentArtifact);

        try {
            Document res = artifact.describe(data, cc);

            if (postDescribeHooks != null) {
                for (Hook hook: postDescribeHooks) {
                    hook.execute(artifact, cc, res);
                }
            }
            return res;
        }
        finally {
            cc.postCall();
        }
    }


    public Artifact getRawArtifact(String identifier)
    throws ArtifactDatabaseException
    {
        PersistentArtifact artifact = backend.getArtifact(identifier);

        if (artifact == null) {
            throw new ArtifactDatabaseException(NO_SUCH_ARTIFACT);
        }

        return artifact.getArtifact();
    }


    public Document describe(
        String   identifier,
        Document data,
        CallMeta callMeta
    )
    throws ArtifactDatabaseException
    {
        // TODO: Handle background tasks
        PersistentArtifact artifact = backend.getArtifact(identifier);

        if (artifact == null) {
            throw new ArtifactDatabaseException(NO_SUCH_ARTIFACT);
        }

        ArtifactCallContext cc = new ArtifactCallContext(
            ArtifactDatabaseImpl.this,
            CallContext.TOUCH,
            callMeta,
            artifact);

        try {
            Artifact art = artifact.getArtifact();
            Document res = art.describe(data, cc);

            if (postDescribeHooks != null) {
                for (Hook hook: postDescribeHooks) {
                    hook.execute(art, cc, res);
                }
            }

            return res;
        }
        finally {
            cc.postCall();
        }
    }

    public Document advance(
        String   identifier,
        Document target,
        CallMeta callMeta
    )
    throws ArtifactDatabaseException
    {
        // TODO: Handle background tasks
        PersistentArtifact artifact = backend.getArtifact(identifier);

        if (artifact == null) {
            throw new ArtifactDatabaseException(NO_SUCH_ARTIFACT);
        }

        ArtifactCallContext cc = new ArtifactCallContext(
            ArtifactDatabaseImpl.this,
            CallContext.STORE,
            callMeta,
            artifact);

        try {
            Artifact art = artifact.getArtifact();
            Document res = art.advance(target, cc);

            if (postAdvanceHooks != null) {
                for (Hook hook: postAdvanceHooks) {
                    hook.execute(art, cc, res);
                }
            }

            return res;
        }
        finally {
            cc.postCall();
        }
    }

    public Document feed(String identifier, Document data, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        // TODO: Handle background tasks
        PersistentArtifact artifact = backend.getArtifact(identifier);

        if (artifact == null) {
            throw new ArtifactDatabaseException(NO_SUCH_ARTIFACT);
        }

        ArtifactCallContext cc = new ArtifactCallContext(
            ArtifactDatabaseImpl.this,
            CallContext.STORE,
            callMeta,
            artifact);

        try {
            Artifact art = artifact.getArtifact();
            Document res = art.feed(data, cc);

            if (postFeedHooks != null) {
                for (Hook hook: postFeedHooks) {
                    hook.execute(art, cc, res);
                }
            }

            return res;
        }
        finally {
            cc.postCall();
        }
    }

    public DeferredOutput out(
        String   identifier,
        Document format,
        CallMeta callMeta)
    throws ArtifactDatabaseException
    {
        return out(identifier, null, format, callMeta);
    }

    public DeferredOutput out(
        String   identifier,
        String   type,
        Document format,
        CallMeta callMeta
    )
    throws ArtifactDatabaseException
    {
        // TODO: Handle background tasks
        PersistentArtifact artifact = backend.getArtifact(identifier);

        if (artifact == null) {
            throw new ArtifactDatabaseException(NO_SUCH_ARTIFACT);
        }

        return new DeferredOutputImpl(artifact, type, format, callMeta);
    }

    public Document exportArtifact(String artifact, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        final String [] factoryName = new String[1];

        byte [] bytes = (byte [])backend.loadArtifact(
            artifact,
            new Backend.ArtifactLoader() {
                public Object load(
                    ArtifactFactory factory,
                    Long            ttl,
                    byte []         bytes,
                    int             id
                ) {
                    factoryName[0] = factory.getName();

                    ArtifactSerializer serializer = factory.getSerializer();

                    Artifact artifact = serializer.fromBytes(bytes);
                    artifact.cleanup(context);

                    return serializer.toBytes(artifact);
                }
            });

        if (bytes == null) {
            throw new ArtifactDatabaseException(NO_SUCH_ARTIFACT);
        }

        return createExportDocument(
            factoryName[0],
            bytes,
            exportSecret);
    }

    /**
     * Creates an exteral XML representation of an artifact.
     * @param factoryName The name of the factory which is responsible
     * for the serialized artifact.
     * @param artifact The byte data of the artifact itself.
     * @param secret   The signing secret.
     * @return An XML document containing the external representation
     * of the artifact.
     */
    protected static Document createExportDocument(
        String  factoryName,
        byte [] artifact,
        byte [] secret
    ) {
        Document document = XMLUtils.newDocument();

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        }
        catch (NoSuchAlgorithmException nsae) {
            logger.error(nsae.getLocalizedMessage(), nsae);
            return document;
        }

        md.update(artifact);
        md.update(secret);

        String checksum = Hex.encodeHexString(md.digest());

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("action");
        document.appendChild(root);

        Element type = ec.create("type");
        ec.addAttr(type, "name", "export", true);
        root.appendChild(type);

        Element data = ec.create("data");
        ec.addAttr(data, "checksum", checksum, true);
        ec.addAttr(data, "factory",  factoryName, true);
        data.setTextContent(Base64.encodeBase64String(artifact));

        root.appendChild(data);

        return document;
    }

    public Document importArtifact(Document input, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        String factoryName = XMLUtils.xpathString(
            input,
            XPATH_IMPORT_FACTORY,
            ArtifactNamespaceContext.INSTANCE);

        ArtifactFactory factory;

        if (factoryName == null
        || (factoryName = factoryName.trim()).length() == 0
        || (factory = getArtifactFactory(factoryName)) == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        String checksumString = XMLUtils.xpathString(
            input,
            XPATH_IMPORT_CHECKSUM,
            ArtifactNamespaceContext.INSTANCE);

        byte [] checksum;

        if (checksumString == null
        || (checksumString = checksumString.trim()).length() == 0
        || (checksum = StringUtils.decodeHex(checksumString)) == null
        ) {
            throw new ArtifactDatabaseException(INVALID_CHECKSUM);
        }

        checksumString = null;

        String dataString = XMLUtils.xpathString(
            input,
            XPATH_IMPORT_DATA,
            ArtifactNamespaceContext.INSTANCE);

        if (dataString == null
        || (dataString = dataString.trim()).length() == 0) {
            throw new ArtifactDatabaseException(NO_DATA);
        }

        byte [] data = Base64.decodeBase64(dataString);

        dataString = null;

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        }
        catch (NoSuchAlgorithmException nsae) {
            logger.error(nsae.getLocalizedMessage(), nsae);
            return XMLUtils.newDocument();
        }

        md.update(data);
        md.update(exportSecret);

        byte [] digest = md.digest();

        if (!Arrays.equals(checksum, digest)) {
            throw new ArtifactDatabaseException(CHECKSUM_MISMATCH);
        }

        ArtifactSerializer serializer = factory.getSerializer();

        Artifact artifact = serializer.fromBytes(data); data = null;

        if (artifact == null) {
            throw new ArtifactDatabaseException(INVALID_ARTIFACT);
        }

        artifact.setIdentifier(backend.newIdentifier());
        PersistentArtifact persistentArtifact;

        try {
            persistentArtifact = backend.storeOrReplace(
                artifact,
                factory,
                factory.timeToLiveUntouched(artifact, context));
        }
        catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new ArtifactDatabaseException(CREATION_FAILED);
        }

        ArtifactCallContext cc = new ArtifactCallContext(
            ArtifactDatabaseImpl.this,
            CallContext.NOTHING,
            callMeta,
            persistentArtifact);

        try {
            return artifact.describe(input, cc);
        }
        finally {
            cc.postCall();
        }
    }

    public String [][] serviceNamesAndDescriptions() {
        return serviceNamesAndDescription;
    }

    public Service.Output process(
        String   serviceName,
        Document input,
        CallMeta callMeta
    )
    throws ArtifactDatabaseException
    {
        Service service = (Service)name2service.get(serviceName);

        if (service == null) {
            throw new ArtifactDatabaseException(NO_SUCH_SERVICE);
        }

        return service.process(input, context, callMeta);
    }

    // User API

    /** Returns user(s) elements. */
    public Document listUsers(CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        UserFactory factory = getUserFactory();

        if (factory == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        User [] users = backend.getUsers(factory, context);

        if (users != null) {
            logger.debug(users.length + " users found in the backend.");
        }

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("users");
        result.appendChild(root);

        if(users != null) {
            for (User user: users) {
                Element ue = ec.create("user");
                ec.addAttr(ue, "uuid", user.identifier(), true);
                ec.addAttr(ue, "name", user.getName(), true);
                Element ua = ec.create("account");
                ec.addAttr(ua, "name", user.getAccount(), true);
                ue.appendChild(ua);

                Document role = user.getRole();

                if (role != null) {
                    ue.appendChild(result.importNode(role.getFirstChild(), true));
                }

                root.appendChild(ue);
            }
        }

        return result;
    }

    /** Search for a user. */
    public Document findUser(Document data, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        UserFactory factory = getUserFactory();

        if (factory == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        String account = XMLUtils.xpathString(
            data, XPATH_USERACCOUNT_FIND, ArtifactNamespaceContext.INSTANCE);

        if (account == null || account.length() == 0) {
            logger.warn("Can't find user without account!");
            throw new ArtifactDatabaseException(NO_USERACCOUNT);
        }

        User user = backend.findUser(account, factory, context);

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element ue = ec.create("user");

        if (user != null) {
            logger.debug(user + " user found in the backend.");

            ec.addAttr(ue, "uuid", user.identifier(), true);
            ec.addAttr(ue, "name", user.getName(), true);
            Element ua = ec.create("account");
            ec.addAttr(ua, "name", user.getAccount(), true);
            ue.appendChild(ua);

            Document role = user.getRole();

            if (role != null) {
                ue.appendChild(result.importNode(role.getFirstChild(), true));
            }
        }

        result.appendChild(ue);

        return result;
    }

    public Document createUser(Document data, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        UserFactory factory = getUserFactory();

        if (factory == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        String name = XMLUtils.xpathString(
            data, XPATH_USERNAME, ArtifactNamespaceContext.INSTANCE);

        if (name == null || name.length() == 0) {
            logger.warn("User without username not accepted!");
            throw new ArtifactDatabaseException(NO_USERNAME);
        }

        String account = XMLUtils.xpathString(
            data, XPATH_USERACCOUNT, ArtifactNamespaceContext.INSTANCE);

        if (account == null || account.length() == 0) {
            logger.warn("User without account not accepted!");
            throw new ArtifactDatabaseException(NO_USERACCOUNT);
        }

        Node tmp = (Node) XMLUtils.xpath(
            data,
            XPATH_USERROLE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        Document role = XMLUtils.newDocument();

        if (tmp != null) {
            Node    clone = role.importNode(tmp, true);
            role.appendChild(clone);
        }

        User newUser = null;

        try {
            newUser = backend.createUser(name, account, role, userFactory, context);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ArtifactDatabaseException(USER_CREATION_FAILED);
        }

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");

        if (newUser != null) {
            root.setTextContent(OPERATION_SUCCESSFUL);
        }
        else {
            root.setTextContent(OPERATION_FAILURE);
        }

        result.appendChild(root);

        return result;
    }

    public Document deleteUser(String userId, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        logger.debug("Delete user: " + userId);

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        boolean success = backend.deleteUser(userId);

        root.setTextContent(success ? OPERATION_SUCCESSFUL: OPERATION_FAILURE);

        return result;
    }


    // Collection API

    public Document getCollectionsMasterArtifact(
        String collectionId,
        CallMeta meta)
        throws ArtifactDatabaseException
    {
        Document result = XMLUtils.newDocument();
        String masterUUID = backend.getMasterArtifact(collectionId);

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        ArtifactCollectionFactory acf = getArtifactCollectionFactory();

        if (acf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        UserFactory uf = getUserFactory();
        if (uf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        ArtifactCollection c = backend.getCollection(
            collectionId, acf, uf, context);

        if (c == null) {
            logger.warn("No collection found with identifier: " + collectionId);
            throw new ArtifactDatabaseException(NO_SUCH_COLLECTION);
        }

        Element root = ec.create("artifact-collection");
        ec.addAttr(root, "name", c.getName(), true);
        ec.addAttr(root, "uuid", c.identifier(), true);
        ec.addAttr(root, "ttl",  String.valueOf(c.getTTL()), true);

        Date creationTime = c.getCreationTime();
        String creation   = creationTime != null
            ? Long.toString(creationTime.getTime())
            : "";

        ec.addAttr(root, "creation", creation,  true);
        result.appendChild(root);

        if (masterUUID == null || masterUUID.length() == 0) {
            logger.debug("No master for the collection existing.");
            return result;
        }

        Element master = ec.create("artifact");
        ec.addAttr(master, "uuid", masterUUID, true);

        root.appendChild(master);

        return result;
    }

    public Document listCollections(String userId, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        ArtifactCollectionFactory acf = getArtifactCollectionFactory();
        UserFactory               uf  = getUserFactory();

        if (acf == null || uf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        logger.debug("Fetch the list of collection for user: " + userId);

        ArtifactCollection [] ac = backend.listCollections(
            userId,
            null, // XXX: fetch from REST
            acf, uf,
            context);

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("artifact-collections");
        result.appendChild(root);

        if (ac == null || ac.length == 0) {
            logger.debug("No collections for the user existing.");

            return result;
        }

        logger.debug("Found " + ac.length + " collections of the user.");

        for (ArtifactCollection c: ac) {
            Element collection = ec.create("artifact-collection");
            ec.addAttr(collection, "name", c.getName(), true);
            ec.addAttr(collection, "uuid", c.identifier(), true);
            ec.addAttr(collection, "ttl",  String.valueOf(c.getTTL()), true);

            Date creationTime = c.getCreationTime();
            String creation   = creationTime != null
                ? Long.toString(creationTime.getTime())
                : "";

            ec.addAttr(collection, "creation", creation,  true);

            root.appendChild(collection);
        }

        return result;
    }

    public Document createCollection(String ownerId, Document data,
        CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        ArtifactCollectionFactory acf = getArtifactCollectionFactory();

        if (acf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        String name = XMLUtils.xpathString(
            data, XPATH_COLLECTION_NAME, ArtifactNamespaceContext.INSTANCE);

        logger.debug("Create new collection with name: " + name);

        Document attr = null;

        Node attrNode = (Node) XMLUtils.xpath(
            data,
            XPATH_COLLECTION_ATTRIBUTE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (attrNode != null) {
            attr = XMLUtils.newDocument();
            attr.appendChild(attr.importNode(attrNode, true));
        }

        ArtifactCollection ac = backend.createCollection(
            ownerId, name, acf, attr, context);

        if (ac == null) {
            throw new ArtifactDatabaseException(COLLECTION_CREATION_FAILED);
        }

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        Element acElement = ec.create("artifact-collection");
        ec.addAttr(acElement, "uuid", ac.identifier(), true);
        ec.addAttr(acElement, "ttl", String.valueOf(ac.getTTL()), true);

        root.appendChild(acElement);

        return result;
    }

    public Document deleteCollection(String collectionId, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        logger.debug("Delete collection: " + collectionId);

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        boolean success = backend.deleteCollection(collectionId);

        root.setTextContent(success ? OPERATION_SUCCESSFUL: OPERATION_FAILURE);

        return result;
    }

    public Document describeCollection(String collectionId, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        logger.debug("Describe collection: " + collectionId);
        ArtifactCollectionFactory acf = getArtifactCollectionFactory();

        if (acf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        UserFactory uf = getUserFactory();
        if (uf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        ArtifactCollection c = backend.getCollection(
            collectionId, acf, uf, context);

        if (c == null) {
            logger.warn("No collection found with identifier: " + collectionId);
            throw new ArtifactDatabaseException(NO_SUCH_COLLECTION);
        }

        CollectionCallContext cc = new CollectionCallContext(
            ArtifactDatabaseImpl.this,
            CallContext.NOTHING,
            callMeta,
            c);

        try {
            return c.describe(cc);
        }
        finally {
            cc.postCall();
        }
    }


    public Document getCollectionAttribute(String collectionId, CallMeta meta)
    throws ArtifactDatabaseException
    {
        logger.debug("Fetch collection attribute for: " + collectionId);

        return backend.getCollectionAttribute(collectionId);
    }


    public Document setCollectionAttribute(
        String   collectionId,
        CallMeta meta,
        Document attribute)
    throws ArtifactDatabaseException
    {
        logger.debug("Set new attribute for the collection: " + collectionId);

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        boolean success = backend.setCollectionAttribute(
            collectionId, attribute);

        root.setTextContent(success ? OPERATION_SUCCESSFUL: OPERATION_FAILURE);

        return result;
    }

    public Document getCollectionItemAttribute(String collectionId, String artifactId,
        CallMeta callMeta) throws ArtifactDatabaseException
    {
        logger.debug("Fetch the attribute for the artifact: " + artifactId);

        return backend.getCollectionItemAttribute(collectionId, artifactId);
    }

    public Document setCollectionItemAttribute(String collectionId, String artifactId,
        Document source, CallMeta callMeta)
        throws ArtifactDatabaseException
    {
        logger.debug("Set the attribute for the artifact: " + artifactId);

        Document attribute = null;

        Node attr = (Node) XMLUtils.xpath(
            source,
            XPATH_COLLECTION_ITEM_ATTRIBUTE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (attr != null) {
            attribute = XMLUtils.newDocument();
            attribute.appendChild(attribute.importNode(attr, true));
        }

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        boolean success = backend.setCollectionItemAttribute(
            collectionId, artifactId, attribute);

        root.setTextContent(success ? OPERATION_SUCCESSFUL: OPERATION_FAILURE);

        return result;
    }

    public Document addCollectionArtifact(
        String   collectionId,
        String   artifactId,
        Document input,
        CallMeta callMeta)
    throws ArtifactDatabaseException
    {
        logger.debug(
            "Add artifact '" + artifactId + "' collection '" +collectionId+"'");

        Document attr = XMLUtils.newDocument();

        Node attrNode = (Node) XMLUtils.xpath(
            input,
            XPATH_COLLECTION_ITEM_ATTRIBUTE,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (attrNode != null) {
            attr.appendChild(attr.importNode(attrNode, true));
        }

        boolean success = backend.addCollectionArtifact(
            collectionId,
            artifactId,
            attr);

        if (!success) {
            Document result = XMLUtils.newDocument();

            XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
                result,
                ArtifactNamespaceContext.NAMESPACE_URI,
                ArtifactNamespaceContext.NAMESPACE_PREFIX);

            Element root = ec.create("result");
            result.appendChild(root);

            root.setTextContent(OPERATION_FAILURE);

            return result;
        }

        return describeCollection(collectionId, callMeta);
    }

    public Document removeCollectionArtifact(String collectionId, String artifactId,
        CallMeta callMeta) throws ArtifactDatabaseException
    {
        logger.debug(
            "Remove artifact '" + artifactId + "' from collection '" +
            collectionId + "'");

        Document attr = XMLUtils.newDocument();

        boolean success = backend.removeCollectionArtifact(
            collectionId,
            artifactId);

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        root.setTextContent(success ? OPERATION_SUCCESSFUL: OPERATION_FAILURE);

        return result;
    }

    public Document listCollectionArtifacts(String collectionId,
        CallMeta callMeta) throws ArtifactDatabaseException
    {
        CollectionItem[] items = backend.listCollectionArtifacts(collectionId);

        Document result = XMLUtils.newDocument();

        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        Element ac   = ec.create("artifact-collection");
        ec.addAttr(ac, "uuid", collectionId, true);

        for (CollectionItem item: items) {
            Element i    = ec.create("collection-item");
            Element attr = ec.create("attribute");
            ec.addAttr(i, "uuid", item.getArtifactIdentifier(), true);

            Document attribute = item.getAttribute();
            if (attribute != null) {
                Node firstChild = attribute.getFirstChild();
                attr.appendChild(result.importNode(firstChild, true));
            }
            else {
                logger.debug("No attributes for the collection item!");
            }

            i.appendChild(attr);
            ac.appendChild(i);
        }

        root.appendChild(ac);
        result.appendChild(root);

        return result;
    }

    public Document setCollectionTTL(String uuid, Document doc, CallMeta meta)
    throws ArtifactDatabaseException
    {
        Document result            = XMLUtils.newDocument();
        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        String tmp = XMLUtils.xpathString(
            doc, XPATH_COLLECTION_TTL, ArtifactNamespaceContext.INSTANCE);

        logger.info("Set TTL of artifact collection '" + uuid + "' to: " + tmp);

        if (tmp == null || tmp.length() == 0) {
            logger.warn("No ttl for this collection specified.");
            root.setTextContent(OPERATION_FAILURE);

            return result;
        }

        Long ttl = null;
        if ((tmp = tmp.toUpperCase()).equals("INF")) {
            ttl = null;
        }
        else if (tmp.equals("DEFAULT")) {
            ArtifactCollectionFactory acf = getArtifactCollectionFactory();
            ttl = acf.timeToLiveUntouched(null, context);
        }
        else {
            try {
                ttl = Long.valueOf(tmp);

                if (ttl < 0) {
                    throw new NumberFormatException("Negative value.");
                }
            }
            catch (NumberFormatException nfe) {
                logger.error("Could not determine TTL", nfe);
                root.setTextContent(OPERATION_FAILURE);
                return result;
            }
        }

        boolean success = backend.setCollectionTTL(uuid, ttl);
        root.setTextContent(success ? OPERATION_SUCCESSFUL: OPERATION_FAILURE);

        return result;
    }


    public Document setCollectionName(String uuid, Document doc, CallMeta meta)
    throws ArtifactDatabaseException
    {
        Document result            = XMLUtils.newDocument();
        XMLUtils.ElementCreator ec = new XMLUtils.ElementCreator(
            result,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("result");
        result.appendChild(root);

        String name = XMLUtils.xpathString(
            doc, XPATH_COLLECTION_NAME, ArtifactNamespaceContext.INSTANCE);

        logger.info("Set name of collection '" + uuid + "' to: " + name);

        if (name == null || name.length() == 0) {
            logger.warn("The new name is emtpy. No new name set!");
            root.setTextContent(OPERATION_FAILURE);
            return result;
        }

        boolean success = backend.setCollectionName(uuid, name);
        root.setTextContent(success ? OPERATION_SUCCESSFUL: OPERATION_FAILURE);

        return result;
    }


    public DeferredOutput outCollection(
        String   collectionId,
        Document format,
        CallMeta callMeta)
    throws ArtifactDatabaseException
    {
        return outCollection(collectionId, null, format, callMeta);
    }

    public DeferredOutput outCollection(
        String   collectionId,
        String   type,
        Document format,
        CallMeta callMeta)
    throws ArtifactDatabaseException
    {
        ArtifactCollectionFactory acf = getArtifactCollectionFactory();

        if (acf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        UserFactory uf = getUserFactory();
        if (uf == null) {
            throw new ArtifactDatabaseException(NO_SUCH_FACTORY);
        }

        ArtifactCollection c = backend.getCollection(
            collectionId, acf, uf, context);

        if (c == null) {
            logger.warn("No collection found with identifier: " + collectionId);
            throw new ArtifactDatabaseException(NO_SUCH_COLLECTION);
        }

        return new DeferredCollectionOutputImpl(c, type, format, callMeta);
    }

    protected void initCallContext(CallContext cc) {
        logger.debug("initCallContext");
        if (callContextListener != null) {
            callContextListener.init(cc);
        }
    }

    protected void closeCallContext(CallContext cc) {
        logger.debug("closeCallContext");
        if (callContextListener != null) {
            callContextListener.close(cc);
        }
    }

    @Override
    public void loadAllArtifacts(ArtifactLoadedCallback callback)
        throws ArtifactDatabaseException
    {
        logger.debug("loadAllArtifacts");
        boolean success = backend.loadAllArtifacts(callback);
        if (!success) {
            throw new ArtifactDatabaseException(INTERNAL_ERROR);
        }
    }

    public void start() {
        if (lifetimeListeners == null || lifetimeListeners.isEmpty()) {
            return;
        }

        for (LifetimeListener ltl: lifetimeListeners) {
            ltl.systemUp(context);
        }

        logger.debug("all lifetime listeners started");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (LifetimeListener ltl: lifetimeListeners) {
                    ltl.systemDown(context);
                }
            }
        });
    }
    
    @Override
    public String findArtifactUser(final String artifactIdentifier) {
        return backend.findUserName(artifactIdentifier);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
