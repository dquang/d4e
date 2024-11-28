/*
 * Copyright (c) 2010, 2011 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.ArtifactCollectionFactory;
import org.dive4elements.artifacts.ArtifactDatabase.ArtifactLoadedCallback;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.ArtifactSerializer;
import org.dive4elements.artifacts.CollectionItem;
import org.dive4elements.artifacts.User;
import org.dive4elements.artifacts.UserFactory;

import org.dive4elements.artifacts.common.utils.StringUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.LRUCache;

import org.dive4elements.artifactdatabase.db.SQLExecutor;
import org.dive4elements.artifactdatabase.db.SQL;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

/**
 * The backend implements the low level layer used to store artifacts
 * in a SQL database.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class Backend
implements   DatabaseCleaner.ArtifactReviver
{
    private static Logger logger = LogManager.getLogger(Backend.class);

    /**
     * The SQL statement to create new artifact id inside the database.
     */
    public String SQL_NEXT_ID;

    /**
     * The SQL statement to insert an artifact into the database.
     */
    public String SQL_INSERT;

    /**
     * The SQL statement to update some columns of an existing
     * artifact in the database.
     */
    public String SQL_UPDATE;

    /**
     * The SQL statement to touch the access time of an
     * artifact inside the database.
     */
    public String SQL_TOUCH;

    /**
     * The SQL statement to load an artifact by a given
     * identifier from the database.
     */
    public String SQL_LOAD_BY_GID;

    /**
     * The SQL statement to get the database id of an artifact
     * identified by the identifier.
     */
    public String SQL_GET_ID;

    /**
     * The SQL statement to replace the content of an
     * existing artifact inside the database.
     */
    public String SQL_REPLACE;

    // USER SQL

    public String SQL_USERS_NEXT_ID;
    public String SQL_USERS_INSERT;
    public String SQL_USERS_SELECT_ID_BY_GID;
    public String SQL_USERS_SELECT_GID;
    public String SQL_USERS_SELECT_ACCOUNT;
    public String SQL_USERS_DELETE_ID;
    public String SQL_USERS_DELETE_COLLECTIONS;
    public String SQL_USERS_SELECT_ALL;
    public String SQL_USERS_COLLECTIONS;
    public String SQL_USERS_COLLECTION_IDS;
    public String SQL_USERS_DELETE_ALL_COLLECTIONS;
    public String SQL_ARTIFACTS_IN_ONLY_COLLECTION_ONLY;
    public String SQL_OUTDATE_ARTIFACTS_COLLECTION;
    public String SQL_UPDATE_COLLECTION_TTL;
    public String SQL_UPDATE_COLLECTION_NAME;
    public String SQL_OUTDATE_ARTIFACTS_USER;
    public String SQL_DELETE_USER_COLLECTION_ITEMS;
    public String SQL_COLLECTIONS_NEXT_ID;
    public String SQL_COLLECTIONS_INSERT;
    public String SQL_COLLECTIONS_SELECT_USER;
    public String SQL_COLLECTIONS_SELECT_ALL;
    public String SQL_COLLECTIONS_SELECT_GID;
    public String SQL_COLLECTIONS_CREATION_TIME;
    public String SQL_COLLECTIONS_ID_BY_GID;
    public String SQL_COLLECTIONS_OLDEST_ARTIFACT;
    public String SQL_DELETE_COLLECTION_ITEMS;
    public String SQL_DELETE_COLLECTION;
    public String SQL_COLLECTION_CHECK_ARTIFACT;
    public String SQL_COLLECTION_ITEMS_ID_NEXTVAL;
    public String SQL_COLLECTION_ITEMS_INSERT;
    public String SQL_COLLECTION_GET_ATTRIBUTE;
    public String SQL_COLLECTION_SET_ATTRIBUTE;
    public String SQL_COLLECTION_ITEM_GET_ATTRIBUTE;
    public String SQL_COLLECTION_ITEM_SET_ATTRIBUTE;
    public String SQL_COLLECTIONS_TOUCH_BY_GID;
    public String SQL_COLLECTION_ITEM_ID_CID_AID;
    public String SQL_COLLECTION_ITEM_OUTDATE_ARTIFACT;
    public String SQL_COLLECTION_ITEM_DELETE;
    public String SQL_COLLECTIONS_TOUCH_BY_ID;
    public String SQL_COLLECTION_ITEMS_LIST_GID;
    public String SQL_ALL_ARTIFACTS;
    public String SQL_FIND_USER_BY_ARTIFACT;

    /** The singleton.*/
    protected static Backend instance;

    protected SQLExecutor sqlExecutor;

    protected List<BackendListener> listeners;

    protected DBConfig config;

    /**
     * The database cleaner. Reference is stored here because
     * the cleaner is woken up if the backend finds an outdated
     * artifact. This artifact should be removed as soon as
     * possible.
     */
    protected DatabaseCleaner cleaner;

    /**
     * To revive an artifact from the bytes coming from the database
     * we need the artifact factory which references the artifact
     * serializer which is able to do the reviving job.
     */
    protected FactoryLookup   factoryLookup;

    /**
     * Little helper interface to decouple the ArtifactDatabase
     * from the Backend. A ArtifactDatabase should depend on a
     * Backend but a Backend not from an ArtifactDatabase.
     */
    public interface FactoryLookup {

        /**
         * Returns an ArtifactFactory which is bound to a given name.
         * @param factoryName The name of the artifact factory.
         * @return The ArtifactFactory bound to the factory name or
         * null if not matching factory is found.
         */
        ArtifactFactory getArtifactFactory(String factoryName);

    } // interface FactoryLookup

    /**
     * Inner class that brigdes between the persisten form of the
     * artifact and the living one inside the artifact database.
     * After the describe(), feed(), advance() and out() operations
     * of the artifact it must be possible to write to modified artifact
     * back into the database.
     */
    public final class PersistentArtifact
    {
        private int                id;
        private Artifact           artifact;
        private ArtifactSerializer serializer;
        private Long               ttl;

        /**
         * Cronstructor to create a persistent artifact.
         * @param artifact   The living artifact.
         * @param serializer The serializer to store the artifact
         * after the operations.
         * @param ttl The time to life of the artifact.
         * @param id The database id of the artifact.
         */
        public PersistentArtifact(
            Artifact           artifact,
            ArtifactSerializer serializer,
            Long               ttl,
            int                id
        ) {
            this.id         = id;
            this.artifact   = artifact;
            this.serializer = serializer;
            this.ttl        = ttl;
        }

        public int getId() {
            return id;
        }

        /**
         * Returns the wrapped living artifact.
         * @return the living artifact.
         */
        public Artifact getArtifact() {
            return artifact;
        }

        /**
         * Returns the serialized which is able to write a
         * modified artifact back into the database.
         * @return The serializer.
         */
        public ArtifactSerializer getSerializer() {
            return serializer;
        }

        /**
         * The time to life of the artifact.
         * @return The time to live.
         */
        public Long getTTL() {
            return ttl;
        }

        /**
         * Stores the living artifact back into the database.
         */
        public void store() {
            if (logger.isDebugEnabled()) {
                logger.debug("storing artifact id = " + getId());
            }
            Backend.this.store(this);
        }

        /**
         * Only touches the access time of the artifact.
         */
        public void touch() {
            if (logger.isDebugEnabled()) {
                logger.debug("touching artifact id = " + getId());
            }
            Backend.this.touch(this);
        }
    } // class ArtifactWithId

    /**
     * Default constructor
     */
    public Backend() {
        listeners = new CopyOnWriteArrayList<BackendListener>();
    }

    public Backend(DBConfig config) {
        this();
        this.config = config;
        sqlExecutor = new SQLExecutor(config.getDBConnection());
        setupSQL(config.getSQL());
    }

    public SQLExecutor getSQLExecutor() {
        return sqlExecutor;
    }

    /**
     * Constructor to create a backend with a link to the database cleaner.
     * @param cleaner The clean which periodically removes outdated
     * artifacts from the database.
     */
    public Backend(DBConfig config, DatabaseCleaner cleaner) {
        this(config);
        this.cleaner = cleaner;
    }

    public DBConfig getConfig() {
        return config;
    }

    /**
     * Returns the singleton of this Backend.
     *
     * @return the backend.
     */
    public static synchronized Backend getInstance() {
        if (instance == null) {
            instance = new Backend(DBConfig.getInstance());
        }

        return instance;
    }

    protected void setupSQL(SQL sql) {
        SQL_NEXT_ID = sql.get("artifacts.id.nextval");
        SQL_INSERT = sql.get("artifacts.insert");
        SQL_UPDATE = sql.get("artifacts.update");
        SQL_TOUCH = sql.get("artifacts.touch");
        SQL_LOAD_BY_GID = sql.get("artifacts.select.gid");
        SQL_GET_ID = sql.get("artifacts.get.id");
        SQL_REPLACE = sql.get("artifacts.replace");
        SQL_USERS_NEXT_ID = sql.get("users.id.nextval");
        SQL_USERS_INSERT = sql.get("users.insert");
        SQL_USERS_SELECT_ID_BY_GID = sql.get("users.select.id.by.gid");
        SQL_USERS_SELECT_GID = sql.get("users.select.gid");
        SQL_USERS_SELECT_ACCOUNT = sql.get("users.select.account");
        SQL_USERS_DELETE_ID = sql.get("users.delete.id");
        SQL_USERS_DELETE_COLLECTIONS = sql.get("users.delete.collections");
        SQL_USERS_SELECT_ALL = sql.get("users.select.all");
        SQL_USERS_COLLECTIONS = sql.get("users.collections");
        SQL_USERS_COLLECTION_IDS = sql.get("users.collection.ids");
        SQL_USERS_DELETE_ALL_COLLECTIONS =
            sql.get("users.delete.collections");
        SQL_ARTIFACTS_IN_ONLY_COLLECTION_ONLY =
            sql.get("artifacts.in.one.collection.only");
        SQL_OUTDATE_ARTIFACTS_COLLECTION =
            sql.get("outdate.artifacts.collection");
        SQL_UPDATE_COLLECTION_TTL = sql.get("collections.update.ttl");
        SQL_UPDATE_COLLECTION_NAME = sql.get("collections.update.name");
        SQL_OUTDATE_ARTIFACTS_USER = sql.get("outdate.artifacts.user");
        SQL_DELETE_USER_COLLECTION_ITEMS =
            sql.get("delete.user.collection.items");
        SQL_COLLECTIONS_NEXT_ID = sql.get("collections.id.nextval");
        SQL_COLLECTIONS_INSERT = sql.get("collections.insert");
        SQL_COLLECTIONS_SELECT_USER = sql.get("collections.select.user");
        SQL_COLLECTIONS_SELECT_ALL = sql.get("collections.select.all");
        SQL_COLLECTIONS_SELECT_GID = sql.get("collections.select.by.gid");
        SQL_COLLECTIONS_CREATION_TIME = sql.get("collection.creation.time");
        SQL_COLLECTIONS_OLDEST_ARTIFACT = sql.get("collections.artifacts.oldest");
        SQL_COLLECTIONS_ID_BY_GID = sql.get("collections.id.by.gid");
        SQL_DELETE_COLLECTION_ITEMS = sql.get("delete.collection.items");
        SQL_DELETE_COLLECTION = sql.get("delete.collection");
        SQL_COLLECTION_CHECK_ARTIFACT = sql.get("collection.check.artifact");
        SQL_COLLECTION_ITEMS_ID_NEXTVAL =
            sql.get("collection.items.id.nextval");
        SQL_COLLECTION_ITEMS_INSERT = sql.get("collection.items.insert");
        SQL_COLLECTION_GET_ATTRIBUTE = sql.get("collection.get.attribute");
        SQL_COLLECTION_SET_ATTRIBUTE = sql.get("collection.set.attribute");
        SQL_COLLECTION_ITEM_GET_ATTRIBUTE =
            sql.get("collection.item.get.attribute");
        SQL_COLLECTION_ITEM_SET_ATTRIBUTE =
            sql.get("collection.item.set.attribute");
        SQL_COLLECTIONS_TOUCH_BY_GID = sql.get("collections.touch.by.gid");
        SQL_COLLECTION_ITEM_ID_CID_AID = sql.get("collection.item.id.cid.aid");
        SQL_COLLECTION_ITEM_OUTDATE_ARTIFACT =
            sql.get("collection.item.outdate.artifact");
        SQL_COLLECTION_ITEM_DELETE = sql.get("collection.item.delete");
        SQL_COLLECTIONS_TOUCH_BY_ID = sql.get("collections.touch.by.id");
        SQL_COLLECTION_ITEMS_LIST_GID = sql.get("collection.items.list.gid");
        SQL_ALL_ARTIFACTS = sql.get("all.artifacts");
        SQL_FIND_USER_BY_ARTIFACT = sql.get("find.user.by.artifact");        
    }

    public void addListener(BackendListener listener) {
        listeners.add(listener);
        logger.debug("# listeners: " + listeners.size());
    }

    public void addAllListeners(List<BackendListener> others) {
        listeners.addAll(others);
        logger.debug("# listeners: " + listeners.size());
    }

    /**
     * Sets the factory lookup mechanism to decouple ArtifactDatabase
     * and Backend.
     * @param factoryLookup
     */
    public void setFactoryLookup(FactoryLookup factoryLookup) {
        this.factoryLookup = factoryLookup;
    }

    /**
     * Sets the database cleaner explicitly.
     * @param cleaner The database cleaner
     */
    public void setCleaner(DatabaseCleaner cleaner) {
        this.cleaner = cleaner;
    }

    /**
     * Returns a new unique identifier to external identify
     * the artifact across the system. This implementation
     * uses random UUIDs v4 to achieve this target.
     * @return the new identifier
     */
    public String newIdentifier() {
        // TODO: check database for collisions.
        return StringUtils.newUUID();
    }

    public boolean isValidIdentifier(String identifier) {
        return StringUtils.checkUUID(identifier);
    }

    /**
     * Stores a new artifact into the database.
     * @param artifact The artifact to be stored
     * @param factory  The factory which build the artifact
     * @param ttl      The initial time to life of the artifact.
     * @return         A persistent wrapper around the living
     * artifact to be able to write modification later.
     * @throws Exception Thrown if something went wrong with the
     * storage process.
     */
    public PersistentArtifact storeInitially(
        Artifact        artifact,
        ArtifactFactory factory,
        Long            ttl
    )
    throws Exception
    {
        return new PersistentArtifact(
            artifact,
            factory.getSerializer(),
            ttl,
            insertDatabase(artifact, factory, ttl));
    }

    /**
     * Stores an artifact into database if it does not exist there.
     * If it exists there it is only updated.
     * @param artifact The artifact to store/update.
     * @param factory The factory which created the artifact.
     * @param ttl The initial time to live of the artifact.
     * @return A persistent version of the artifact to be able
     * to store a modification later.
     * @throws Exception Thrown if something went wrong during
     * storing/updating.
     */
    public PersistentArtifact storeOrReplace(
        Artifact        artifact,
        ArtifactFactory factory,
        Long            ttl
    )
    throws Exception
    {
        return new PersistentArtifact(
            artifact,
            factory.getSerializer(),
            ttl,
            storeOrReplaceDatabase(artifact, factory, ttl));
    }

    /**
     * Implementors of this interface are able to process the raw
     * artifact data from the database for loading.
     */
    public interface ArtifactLoader {

        /**
         * Creates a custom object from the raw artifact database data.
         * @param factory The factory that created this artifact.
         * @param ttl The current time to life of the artifact.
         * @param bytes The raw artifact bytes from the database.
         * @param id The database id of the artifact.
         * @return The custom object created by the implementation.
         */
        Object load(ArtifactFactory factory, Long ttl, byte [] bytes, int id);

    } // interface ArtifactLoader

    /**
     * Fetches an artifact from the database identified by the
     * given identifier.
     * @param identifer The identifier of the artifact.
     * @return A persistent wrapper around the found artifact
     * to be able to write back a modifaction later or null
     * if no artifact is found for this identifier.
     */
    public PersistentArtifact getArtifact(String identifer) {

        return (PersistentArtifact)loadArtifact(
            identifer,
            new ArtifactLoader() {

                public Object load(
                    ArtifactFactory factory,
                    Long            ttl,
                    byte []         bytes,
                    int             id
                ) {
                    ArtifactSerializer serializer = factory.getSerializer();

                    Artifact artifact = serializer.fromBytes(bytes);

                    return artifact == null
                        ? null
                        : new PersistentArtifact(artifact, serializer, ttl, id);
                }
            });
    }

    /**
     * More general loading mechanism for artifacts. The concrete
     * load processing is delegated to the given loader.
     * @param identifer The identifier of the artifact.
     * @param loader The loader which processes the raw database data.
     * @return The object created by the loader.
     */
    public Object loadArtifact(
        final String         identifer,
        final ArtifactLoader loader
    ) {
        if (!isValidIdentifier(identifer)) {
            return null;
        }

        if (factoryLookup == null) {
            logger.error("factory lookup == null");
            return false;
        }

        final Object [] loaded = new Object[1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_LOAD_BY_GID);
                stmnt.setString(1, identifer);

                result = stmnt.executeQuery();

                if (!result.next()) {
                    return false;
                }

                int  id   = result.getInt(1);
                long ttlX = result.getLong(2);
                Long ttl  = result.wasNull() ? null : ttlX;

                String factoryName = result.getString(3);

                ArtifactFactory factory = factoryLookup
                    .getArtifactFactory(factoryName);

                if (factory == null) {
                    logger.error("factory '" + factoryName + "' not found");
                    return false;
                }

                byte [] bytes = result.getBytes(4);

                loaded[0] = loader.load(factory, ttl, bytes, id);
                return true;
            }
        };

        return exec.runRead() ? loaded[0] : null;
    }

    /**
     * Called if the load mechanism found an outdated artifact.
     * It  wakes up the database cleaner.
     * @param id The id of the outdated artifact.
     */
    protected void artifactOutdated(int id) {
        if (logger.isDebugEnabled()) {
            logger.info("artifactOutdated: id = " + id);
        }
        if (cleaner != null) {
            cleaner.wakeup();
        }
    }

    public Artifact reviveArtifact(String factoryName, byte [] bytes) {
        if (factoryLookup == null) {
            logger.error("reviveArtifact: factory lookup == null");
            return null;
        }
        ArtifactFactory factory = factoryLookup
            .getArtifactFactory(factoryName);

        if (factory == null) {
            logger.error(
                "reviveArtifact: no factory '" + factoryName + "' found");
            return null;
        }

        ArtifactSerializer serializer = factory.getSerializer();

        return serializer.fromBytes(bytes);
    }

    /**
     * Internal method to store/replace an artifact inside the database.
     * If an artifact with the given identifier does not exists it is
     * created else only the content data is updated.
     * @param artifact The artifact to be store/update inside the database.
     * @param factory The factory that created the artifact.
     * @param ttl The initial time to life of the artifact.
     * @return The database id of the stored/updated artifact.
     */
    protected int storeOrReplaceDatabase(
        final Artifact        artifact,
        final ArtifactFactory factory,
        final Long            ttl
    ) {
        final String uuid = artifact.identifier();

        if (!isValidIdentifier(uuid)) {
            throw new RuntimeException("No valid UUID");
        }

        final int     [] id     = new int[1];
        final boolean [] stored = new boolean[1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {

                prepareStatement(SQL_GET_ID);
                stmnt.setString(1, uuid);
                result = stmnt.executeQuery();

                Integer ID = result.next()
                    ? Integer.valueOf(result.getInt(1))
                    : null;

                reset();

                if (stored[0] = ID != null) { // already in database
                    prepareStatement(SQL_REPLACE);

                    if (ttl == null) {
                        stmnt.setNull(1, Types.BIGINT);
                    }
                    else {
                        stmnt.setLong(1, ttl.longValue());
                    }

                    stmnt.setString(2, factory.getName());
                    stmnt.setBytes(
                        3,
                        factory.getSerializer().toBytes(artifact));
                    id[0] = ID.intValue();
                    stmnt.setInt(4, id[0]);
                }
                else { // new artifact
                    prepareStatement(SQL_NEXT_ID);
                    result = stmnt.executeQuery();

                    if (!result.next()) {
                        logger.error("No id generated");
                        return false;
                    }

                    reset();

                    prepareStatement(SQL_INSERT);

                    id[0] = result.getInt(1);
                    stmnt.setInt(1, id[0]);
                    stmnt.setString(2, uuid);
                    if (ttl == null) {
                        stmnt.setNull(3, Types.BIGINT);
                    }
                    else {
                        stmnt.setLong(3, ttl.longValue());
                    }

                    stmnt.setString(4, factory.getName());

                    stmnt.setBytes(
                        5,
                        factory.getSerializer().toBytes(artifact));
                }
                stmnt.execute();
                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            throw new RuntimeException("failed insert artifact into database");
        }

        if (stored[0]) {
            fireStoredArtifact(artifact);
        }
        else {
            fireCreatedArtifact(artifact);
        }

        return id[0];
    }

    /**
     * Internal method to store an artifact inside the database.
     * @param artifact The artifact to be stored.
     * @param factory The factory which created the artifact.
     * @param ttl The initial time to live of the artifact.
     * @return The database id of the stored artifact.
     */
    protected int insertDatabase(
        final Artifact        artifact,
        final ArtifactFactory factory,
        final Long            ttl
    ) {
        final int [] id = new int[1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_NEXT_ID);
                result = stmnt.executeQuery();

                if (!result.next()) {
                    logger.error("No id generated");
                    return false;
                }

                id[0] = result.getInt(1);

                reset();
                prepareStatement(SQL_INSERT);

                String uuid = artifact.identifier();
                stmnt.setInt(1, id[0]);
                stmnt.setString(2, uuid);
                if (ttl == null) {
                    stmnt.setNull(3, Types.BIGINT);
                }
                else {
                    stmnt.setLong(3, ttl.longValue());
                }

                stmnt.setString(4, factory.getName());

                stmnt.setBytes(
                    5,
                    factory.getSerializer().toBytes(artifact));

                stmnt.execute();

                conn.commit();
                return true;
            }
        };

        if (!exec.runWrite()) {
            throw new RuntimeException("failed insert artifact into database");
        }

        fireCreatedArtifact(artifact);

        return id[0];
    }

    protected void fireCreatedArtifact(Artifact artifact) {
        for (BackendListener listener: listeners) {
            listener.createdArtifact(artifact, this);
        }
    }

    /**
     * Touches the access timestamp of a given artifact to prevent
     * that it will be removed from the database by the database cleaner.
     * @param artifact The persistent wrapper around the living artifact.
     */
    public void touch(final PersistentArtifact artifact) {
        sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_TOUCH);
                stmnt.setInt(1, artifact.getId());
                stmnt.execute();
                conn.commit();
                return true;
            }
        }.runWrite();
    }

    /**
     * Writes modification of an artifact back to the database.
     * @param artifact The persistent wrapper around a living
     * artifact.
     */
    public void store(final PersistentArtifact artifact) {
        boolean success = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_UPDATE);
                stmnt.setInt(2, artifact.getId());

                byte [] bytes = artifact
                    .getSerializer()
                    .toBytes(artifact.getArtifact());

                stmnt.setBytes(1, bytes);
                stmnt.execute();
                conn.commit();
                return true;
            }
        }.runWrite();

        if (success) {
            fireStoredArtifact(artifact.getArtifact());
        }
    }

    protected void fireStoredArtifact(Artifact artifact) {
        for (BackendListener listener: listeners) {
            listener.storedArtifact(artifact, this);
        }
    }


    public User createUser(
        final String      name,
        final String      account,
        final Document    role,
        final UserFactory factory,
        final Object      context
    ) {
        final User [] user = new User[1];

        final byte [] roleData = XMLUtils.toByteArray(role, true);

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {

                prepareStatement(SQL_USERS_NEXT_ID);
                result = stmnt.executeQuery();

                if (!result.next()) {
                    return false;
                }

                int id = result.getInt(1);

                reset();

                String identifier = newIdentifier();

                prepareStatement(SQL_USERS_INSERT);

                stmnt.setInt(1, id);
                stmnt.setString(2, identifier);
                stmnt.setString(3, name);
                stmnt.setString(4, account);

                if (roleData == null) {
                    stmnt.setNull(5, Types.BINARY);
                }
                else {
                    stmnt.setBytes(5, roleData);
                }

                stmnt.execute();
                conn.commit();

                user[0] = factory.createUser(
                    identifier, name, account, role, context);
                return true;
            }
        };

        boolean success = exec.runWrite();

        if (success) {
            fireCreatedUser(user[0]);
            return user[0];
        }

        return null;
    }

    protected void fireCreatedUser(User user) {
        for (BackendListener listener: listeners) {
            listener.createdUser(user, this);
        }
    }

    public boolean deleteUser(final String identifier) {

        if (!isValidIdentifier(identifier)) {
            return false;
        }

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_USERS_SELECT_ID_BY_GID);

                stmnt.setString(1, identifier);
                result = stmnt.executeQuery();

                if (!result.next()) { // No such user
                    return false;
                }

                int id = result.getInt(1);

                reset();

                // outdate the artifacts exclusively used by the user

                prepareStatement(SQL_OUTDATE_ARTIFACTS_USER);
                stmnt.setInt(1, id);
                stmnt.setInt(2, id);
                stmnt.execute();

                reset();

                // delete the collection items of the user

                prepareStatement(SQL_DELETE_USER_COLLECTION_ITEMS);
                stmnt.setInt(1, id);
                stmnt.execute();

                reset();

                // delete the collections of the user

                prepareStatement(SQL_USERS_DELETE_COLLECTIONS);
                stmnt.setInt(1, id);
                stmnt.execute();

                reset();

                // delete the user

                prepareStatement(SQL_USERS_DELETE_ID);
                stmnt.setInt(1, id);
                stmnt.execute();

                conn.commit();
                return true;
            }
        };

        boolean success = exec.runWrite();

        if (success) {
            fireDeletedUser(identifier);
        }

        return success;
    }

    protected void fireDeletedUser(String identifier) {
        for (BackendListener listener: listeners) {
            listener.deletedUser(identifier, this);
        }
    }

    public User getUser(
        final String      identifier,
        final UserFactory factory,
        final Object      context
    ) {
        if (!isValidIdentifier(identifier)) {
            logger.debug("Invalid UUID: '" + identifier + "'");
            return null;
        }

        final User [] user = new User[1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_USERS_SELECT_GID);
                stmnt.setString(1, identifier);
                result = stmnt.executeQuery();
                if (!result.next()) { // no such user
                    return false;
                }
                // omit id
                String  name     = result.getString(2);
                String account   = result.getString(3);
                byte [] roleData = result.getBytes(4);

                Document role = null;
                if (roleData != null) {
                    role = XMLUtils.fromByteArray(roleData, true);
                }

                user[0] = factory.createUser(
                    identifier, name, account, role, context);
                return true;
            }
        };

        return exec.runRead() ? user[0] : null;
    }

    /**
     * Find/Get user by account.
     */
    public User findUser(
        final String      account,
        final UserFactory factory,
        final Object      context
    ) {

        final User [] user = new User[1];
        logger.debug("Trying to find user by account " + account);

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_USERS_SELECT_ACCOUNT);
                stmnt.setString(1, account);
                result = stmnt.executeQuery();
                if (!result.next()) { // no such user
                    logger.debug("No user found.");
                    return false;
                }
                String  identifier = result.getString(1);
                String  name     = result.getString(2);
                String account   = result.getString(3);
                byte [] roleData = result.getBytes(4);

                Document role = null;
                if (roleData != null) {
                    role = XMLUtils.fromByteArray(roleData, true);
                }

                user[0] = factory.createUser(
                    identifier, name, account, role, context);
                return true;
            }
        };

        return exec.runRead() ? user[0] : null;
    }
    
    /** Find the owner of a given artifact */
    public String findUserName(final String artifactGid) {

        final String[] returnValue = new String[1];

        final SQLExecutor.Instance exec = this.sqlExecutor.new Instance() {

            @Override
            public boolean doIt() throws SQLException {

                prepareStatement(Backend.this.SQL_FIND_USER_BY_ARTIFACT);
                this.stmnt.setString(1, artifactGid);

                this.result = this.stmnt.executeQuery();

                // final HashMap<String, LazyBackendUser> users = new HashMap<String, LazyBackendUser>();

                while (this.result.next()) {
                    // final String userIdentifier = this.result.getString(1);
                    final String userName = this.result.getString(2);

                    // We only need the name at the moment, else we could do this: User user = new LazyBackendUser(
                    // userIdentifier, userFactory, Backend.this, context);
                    returnValue[0] = userName;
                    return true;
                }

                return true;
            }
        };

        if (exec.runRead())
            return returnValue[0];

        return null;
    }    

    public User [] getUsers(
        final UserFactory factory,
        final Object      context
    ) {
        final ArrayList<User> users = new ArrayList<User>();

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_USERS_SELECT_ALL);
                result = stmnt.executeQuery();

                while (result.next()) {
                    // omit id
                    String  identifier = result.getString(2);
                    String  name       = result.getString(3);
                    String  account    = result.getString(4);
                    byte [] roleData   = result.getBytes(5);

                    Document role = XMLUtils.fromByteArray(roleData, true);
                    User user = factory.createUser(
                        identifier, name, account, role, context);
                    users.add(user);
                }
                return true;
            }
        };

        return exec.runRead()
            ? users.toArray(new User[users.size()])
            : null;
    }

    public ArtifactCollection createCollection(
        final String                    ownerIdentifier,
        final String                    name,
        final ArtifactCollectionFactory factory,
        final Document                  attribute,
        final Object                    context
    ) {
        if (name == null) {
            logger.debug("Name is null");
            return null;
        }

        if (!isValidIdentifier(ownerIdentifier)) {
            logger.debug("Invalid owner id: '" + ownerIdentifier + "'");
            return null;
        }

        final ArtifactCollection [] collection = new ArtifactCollection[1];

        final byte [] data = XMLUtils.toByteArray(attribute, true);

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                // fetch owner id
                prepareStatement(SQL_USERS_SELECT_ID_BY_GID);
                stmnt.setString(1, ownerIdentifier);
                result = stmnt.executeQuery();

                if (!result.next()) { // no such user
                    return false;
                }

                int ownerId = result.getInt(1);
                reset();

                // fetch new collection seq number.
                prepareStatement(SQL_COLLECTIONS_NEXT_ID);
                result = stmnt.executeQuery();

                if (!result.next()) { // no identifier generated
                    return false;
                }

                int id = result.getInt(1);
                reset();

                String identifier = newIdentifier();

                prepareStatement(SQL_COLLECTIONS_INSERT);

                stmnt.setInt(1, id);
                stmnt.setString(2, identifier);
                stmnt.setString(3, name);
                stmnt.setInt(4, ownerId);

                // XXX: A bit odd: we don't have a collection, yet.
                Long ttl = factory.timeToLiveUntouched(null, context);

                if (ttl == null) {
                    stmnt.setNull(5, Types.BIGINT);
                }
                else {
                    stmnt.setLong(5, ttl);
                }

                if (data == null) {
                    stmnt.setNull(6, Types.BINARY);
                }
                else {
                    stmnt.setBytes(6, data);
                }

                stmnt.execute();
                conn.commit();

                reset();

                // fetch creation time from database
                // done this way to use the time system
                // of the database.

                prepareStatement(SQL_COLLECTIONS_CREATION_TIME);
                stmnt.setInt(1, id);

                result = stmnt.executeQuery();

                Date creationTime = null;

                if (result.next()) {
                    Timestamp timestamp = result.getTimestamp(1);
                    creationTime = new Date(timestamp.getTime());
                }

                collection[0] = factory.createCollection(
                    identifier, name, creationTime, ttl, attribute, context);

                if (collection[0] != null) {
                    // XXX: Little hack to make the listeners happy
                    collection[0].setUser(new DefaultUser(ownerIdentifier));
                }

                return true;
            }
        };

        boolean success = exec.runWrite();

        if (success) {
            fireCreatedCollection(collection[0]);
            return collection[0];
        }
        return null;
    }

    protected void fireCreatedCollection(ArtifactCollection collection) {
        for (BackendListener listener: listeners) {
            listener.createdCollection(collection, this);
        }
    }

    public ArtifactCollection getCollection(
        final String                    collectionId,
        final ArtifactCollectionFactory collectionFactory,
        final UserFactory               userFactory,
        final Object                    context
    ) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("collection id is not valid: " + collectionId);
            return null;
        }

        final ArtifactCollection[] ac = new ArtifactCollection[1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {

                prepareStatement(SQL_COLLECTIONS_SELECT_GID);
                stmnt.setString(1, collectionId);

                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such collection");
                    return false;
                }

                String collectionName = result.getString(2);
                String ownerId        = result.getString(3);
                Date   creationTime   =
                    new Date(result.getTimestamp(4).getTime());
                Date   lastAccess     =
                    new Date(result.getTimestamp(5).getTime());
                Document attr         =
                    XMLUtils.fromByteArray(result.getBytes(6), true);
                long ttl              = result.getLong(7);

                ArtifactCollection collection =
                    collectionFactory.createCollection(
                        collectionId,
                        collectionName,
                        creationTime,
                        ttl,
                        attr,
                        context);

                if (ownerId != null) {
                    collection.setUser(new LazyBackendUser(
                        ownerId, userFactory, Backend.this, context));
                }

                ac[0] = collection;

                return true;
            }
        };

        return exec.runRead() ? ac[0] : null;
    }

    public ArtifactCollection [] listCollections(
        final String                    ownerIdentifier,
        final Document                  data,
        final ArtifactCollectionFactory collectionFactory,
        final UserFactory               userFactory,
        final Object                    context
    ) {
        if (ownerIdentifier != null
        && !isValidIdentifier(ownerIdentifier)) {
            logger.debug("Invalid owner id: '" + ownerIdentifier + "'");
            return null;
        }

        final ArrayList<ArtifactCollection> collections =
            new ArrayList<ArtifactCollection>();

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {

            public boolean doIt() throws SQLException {

                if (ownerIdentifier != null) {
                    prepareStatement(SQL_COLLECTIONS_SELECT_USER);
                    stmnt.setString(1, ownerIdentifier);
                }
                else {
                    prepareStatement(SQL_COLLECTIONS_SELECT_ALL);
                }

                result = stmnt.executeQuery();

                HashMap<String, LazyBackendUser> users =
                    new HashMap<String, LazyBackendUser>();

                while (result.next()) {
                    String collectionIdentifier = result.getString(1);
                    String collectionName       = result.getString(2);
                    Date   creationTime         =
                        new Date(result.getTimestamp(3).getTime());
                    String userIdentifier       = result.getString(4);
                    long   ttl                  = result.getLong(5);

                    ArtifactCollection collection =
                        collectionFactory.createCollection(
                            collectionIdentifier,
                            collectionName,
                            creationTime,
                            ttl,
                            data,
                            context);

                    if (userIdentifier != null) {
                        LazyBackendUser user = users.get(userIdentifier);
                        if (user == null) {
                            user = new LazyBackendUser(
                                userIdentifier, userFactory,
                                Backend.this, context);
                            users.put(userIdentifier, user);
                        }
                        collection.setUser(user);
                    }

                    collections.add(collection);
                }
                return true;
            }
        };

        return exec.runRead()
            ? collections.toArray(new ArtifactCollection[collections.size()])
            : null;
    }


    public String getMasterArtifact(final String collectionId) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("Invalid collection id: '" + collectionId + "'");
            return null;
        }
        final String [] uuid = new String[1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                // Fetch masters (oldest artifact) id.
                prepareStatement(SQL_COLLECTIONS_OLDEST_ARTIFACT);
                stmnt.setString(1, collectionId);
                stmnt.setMaxRows(1); //
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such collection: " + collectionId);
                    return false;
                }
                uuid[0] = result.getString(1);
                if (logger.isDebugEnabled()) {
                    logger.debug("getMasterArtifact result.getString " +
                        uuid[0]);
                }
                return true;
            }
        };
        return exec.runRead() ? uuid[0] : null;
    }

    public boolean deleteCollection(final String collectionId) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("Invalid collection id: '" + collectionId + "'");
            return false;
        }
        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                // fetch collection id
                prepareStatement(SQL_COLLECTIONS_ID_BY_GID);
                stmnt.setString(1, collectionId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such collection: " + collectionId);
                    return false;
                }
                int id = result.getInt(1);
                reset();

                // outdate artifacts that are only in this collection
                logger.info("Outdate Artifacts that belong to collection: " + id);

                prepareStatement(SQL_OUTDATE_ARTIFACTS_COLLECTION);
                stmnt.setInt(1, id);
                stmnt.setInt(2, id);
                stmnt.execute();
                reset();

                // delete the collection items
                prepareStatement(SQL_DELETE_COLLECTION_ITEMS);
                stmnt.setInt(1, id);
                stmnt.execute();
                reset();

                // delete the collection
                prepareStatement(SQL_DELETE_COLLECTION);
                stmnt.setInt(1, id);
                stmnt.execute();
                conn.commit();
                return true;
            }
        };
        boolean success = exec.runWrite();

        if (success) {
            fireDeletedCollection(collectionId);
        }

        return success;
    }

    protected void fireDeletedCollection(String identifier) {
        for (BackendListener listener: listeners) {
            listener.deletedCollection(identifier, this);
        }
    }

    public Document getCollectionAttribute(final String collectionId) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("collection id is not valid: " + collectionId);
        }

        final byte[][] data = new byte[1][1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_COLLECTION_GET_ATTRIBUTE);
                stmnt.setString(1, collectionId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such collection.");
                    return false;
                }

                data[0] = result.getBytes(1);
                return true;
            }
        };

        return exec.runRead()
            ? XMLUtils.fromByteArray(data[0], true)
            : null;
    }

    public boolean setCollectionAttribute(
        final String   collectionId,
        Document       attribute
    ) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("collection id is not valid: " + collectionId);
            return false;
        }

        final byte [] data = XMLUtils.toByteArray(attribute, true);

        boolean success = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {

                // set the column in collection items
                prepareStatement(SQL_COLLECTION_SET_ATTRIBUTE);
                if (data == null) {
                    stmnt.setNull(1, Types.BINARY);
                }
                else {
                    stmnt.setBytes(1, data);
                }
                stmnt.setString(2, collectionId);
                stmnt.execute();
                reset();

                // touch the collection
                prepareStatement(SQL_COLLECTIONS_TOUCH_BY_GID);
                stmnt.setString(1, collectionId);
                stmnt.execute();

                conn.commit();
                return true;
            }
        }.runWrite();

        if (success) {
            fireChangedCollectionAttribute(collectionId, attribute);
        }

        return success;
    }

    protected void fireChangedCollectionAttribute(
        String   collectionId,
        Document document
    ) {
        for (BackendListener listener: listeners) {
            listener.changedCollectionAttribute(collectionId, document, this);
        }
    }

    public Document getCollectionItemAttribute(
        final String collectionId,
        final String artifactId
    ) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("collection id is not valid: " + collectionId);
            return null;
        }
        if (!isValidIdentifier(artifactId)) {
            logger.debug("artifact id is not valid: " + artifactId);
            return null;
        }

        final byte [][] data = new byte[1][1];

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_COLLECTION_ITEM_GET_ATTRIBUTE);
                stmnt.setString(1, collectionId);
                stmnt.setString(2, artifactId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such collection item");
                    return false;
                }
                data[0] = result.getBytes(1);
                return true;
            }
        };

        return exec.runRead()
            ? XMLUtils.fromByteArray(data[0], true)
            : null;
    }

    public boolean setCollectionItemAttribute(
        final String   collectionId,
        final String   artifactId,
        Document       attribute
    ) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("collection id is not valid: " + collectionId);
            return false;
        }
        if (!isValidIdentifier(artifactId)) {
            logger.debug("artifact id is not valid: " + artifactId);
            return false;
        }

        final byte [] data = XMLUtils.toByteArray(attribute, true);

        boolean success = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {

                // set the column in collection items
                prepareStatement(SQL_COLLECTION_ITEM_SET_ATTRIBUTE);
                if (data == null) {
                    stmnt.setNull(1, Types.BINARY);
                }
                else {
                    stmnt.setBytes(1, data);
                }
                stmnt.setString(2, collectionId);
                stmnt.setString(3, artifactId);
                stmnt.execute();
                reset();

                // touch the collection
                prepareStatement(SQL_COLLECTIONS_TOUCH_BY_GID);
                stmnt.setString(1, collectionId);
                stmnt.execute();

                conn.commit();
                return true;
            }
        }.runWrite();

        if (success) {
            fireChangedCollectionItemAttribute(
                collectionId, artifactId, attribute);
        }

        return success;
    }

    protected void fireChangedCollectionItemAttribute(
        String collectionId,
        String artifactId,
        Document document
    ) {
        for (BackendListener listener: listeners) {
            listener.changedCollectionItemAttribute(
                collectionId, artifactId, document, this);
        }
    }

    public boolean addCollectionArtifact(
        final String   collectionId,
        final String   artifactId,
        final Document attribute
    ) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("Invalid collection id: '" + collectionId + "'");
            return false;
        }

        if (!isValidIdentifier(artifactId)) {
            logger.debug("Invalid artifact id: '" + artifactId + "'");
            return false;
        }

        final byte [] data = XMLUtils.toByteArray(attribute, true);

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                // fetch artifact id
                prepareStatement(SQL_GET_ID);
                stmnt.setString(1, artifactId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such artifact: " + artifactId);
                    return false;
                }
                int aid = result.getInt(1);
                reset();

                // fetch collection id
                prepareStatement(SQL_COLLECTIONS_ID_BY_GID);
                stmnt.setString(1, collectionId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such collection: " + collectionId);
                }
                int cid = result.getInt(1);
                reset();

                // check if artifact is already in collection
                prepareStatement(SQL_COLLECTION_CHECK_ARTIFACT);
                stmnt.setInt(1, aid);
                stmnt.setInt(2, cid);
                result = stmnt.executeQuery();
                if (result.next()) {
                    logger.debug("artifact already in collection");
                    return false;
                }
                reset();

                // fetch fresh id for new collection item
                prepareStatement(SQL_COLLECTION_ITEMS_ID_NEXTVAL);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("no collection item id generated");
                    return false;
                }
                int ci_id = result.getInt(1);
                reset();

                // insert new collection item
                prepareStatement(SQL_COLLECTION_ITEMS_INSERT);
                stmnt.setInt(1, ci_id);
                stmnt.setInt(2, cid);
                stmnt.setInt(3, aid);

                if (data == null) {
                    stmnt.setNull(4, Types.BINARY);
                }
                else {
                    stmnt.setBytes(4, data);
                }
                stmnt.execute();
                conn.commit();

                return true;
            }
        };
        boolean success = exec.runWrite();

        if (success) {
            fireAddedArtifactToCollection(artifactId, collectionId);
        }

        return success;
    }

    protected void fireAddedArtifactToCollection(
        String artifactId,
        String collectionId
    ) {
        for (BackendListener listener: listeners) {
            listener.addedArtifactToCollection(
                artifactId, collectionId, this);
        }
    }

    public boolean removeCollectionArtifact(
        final String collectionId,
        final String artifactId
    ) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("Invalid collection id: '" + collectionId + "'");
            return false;
        }

        boolean success = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {

                // fetch id, collection id and artitfact id
                prepareStatement(SQL_COLLECTION_ITEM_ID_CID_AID);
                stmnt.setString(1, collectionId);
                stmnt.setString(2, artifactId);
                result = stmnt.executeQuery();
                if (!result.next()) {
                    logger.debug("No such collection item");
                    return false;
                }
                int  id = result.getInt(1);
                int cid = result.getInt(2);
                int aid = result.getInt(3);
                reset();

                // outdate artifact iff it is only in this collection
                prepareStatement(SQL_COLLECTION_ITEM_OUTDATE_ARTIFACT);
                stmnt.setInt(1, aid);
                stmnt.setInt(2, cid);
                stmnt.setInt(3, aid);
                stmnt.execute();
                reset();

                // delete collection item
                prepareStatement(SQL_COLLECTION_ITEM_DELETE);
                stmnt.setInt(1, id);
                stmnt.execute();
                reset();

                // touch collection
                prepareStatement(SQL_COLLECTIONS_TOUCH_BY_ID);
                stmnt.setInt(1, cid);
                stmnt.execute();

                conn.commit();
                return true;
            }
        }.runWrite();

        if (success) {
            fireRemovedArtifactFromCollection(artifactId, collectionId);
        }

        return success;
    }

    protected void fireRemovedArtifactFromCollection(
        String artifactId,
        String collectionId
    ) {
        for (BackendListener listener: listeners) {
            listener.removedArtifactFromCollection(
                artifactId, collectionId, this);
        }
    }

    public CollectionItem [] listCollectionArtifacts(
        final String collectionId
    ) {
        if (!isValidIdentifier(collectionId)) {
            logger.debug("Invalid collection id: '" + collectionId + "'");
            return null;
        }

        final ArrayList<CollectionItem> collectionItems =
            new ArrayList<CollectionItem>();

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_COLLECTION_ITEMS_LIST_GID);
                stmnt.setString(1, collectionId);
                result = stmnt.executeQuery();
                while (result.next()) {
                    CollectionItem item = new DefaultCollectionItem(
                        result.getString(1),
                        result.getBytes(2));
                    collectionItems.add(item);
                }
                return true;
            }
        };

        return exec.runRead()
            ? collectionItems.toArray(
                new CollectionItem[collectionItems.size()])
            : null;
    }


    public boolean setCollectionTTL(final String uuid, final Long ttl) {
        if (!isValidIdentifier(uuid)) {
            logger.debug("Invalid collection id: '" + uuid + "'");
            return false;
        }

        return sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_UPDATE_COLLECTION_TTL);
                if (ttl == null) {
                    stmnt.setNull(1, Types.BIGINT);
                }
                else {
                    stmnt.setLong(1, ttl);
                }
                stmnt.setString(2, uuid);
                stmnt.execute();
                conn.commit();

                return true;
            }
        }.runWrite();
    }


    public boolean setCollectionName(final String uuid, final String name) {
        if (!isValidIdentifier(uuid)) {
            logger.debug("Invalid collection id: '" + uuid + "'");
            return false;
        }

        boolean success = sqlExecutor.new Instance() {
            public boolean doIt() throws SQLException {
                prepareStatement(SQL_UPDATE_COLLECTION_NAME);
                stmnt.setString(1, name);
                stmnt.setString(2, uuid);
                stmnt.execute();
                conn.commit();

                return true;
            }
        }.runWrite();

        if (success) {
            fireSetCollectionName(uuid, name);
        }

        return success;
    }

    protected void fireSetCollectionName(String identifier, String name) {
        for (BackendListener listener: listeners) {
            listener.setCollectionName(identifier, name);
        }
    }

    public boolean loadAllArtifacts(final ArtifactLoadedCallback alc) {

        logger.debug("loadAllArtifacts");

        if (factoryLookup == null) {
            logger.error("factory lookup == null");
            return false;
        }

        boolean success = sqlExecutor.new Instance() {
            @Override
            public boolean doIt() throws SQLException {
                // a little cache to avoid too much deserializations.
                LRUCache<String, Artifact> alreadyLoaded =
                    new LRUCache<String, Artifact>(200);

                prepareStatement(SQL_ALL_ARTIFACTS);
                result = stmnt.executeQuery();
                while (result.next()) {
                    String userId         = result.getString("u_gid");
                    String collectionId   = result.getString("c_gid");
                    String collectionName = result.getString("c_name");
                    String artifactId     = result.getString("a_gid");
                    String factoryName    = result.getString("factory");
                    Date collectionCreated =
                        new Date(result.getTimestamp("c_creation").getTime());
                    Date artifactCreated =
                        new Date(result.getTimestamp("a_creation").getTime());

                    Artifact artifact = alreadyLoaded.get(artifactId);

                    if (artifact != null) {
                        alc.artifactLoaded(
                            userId,
                            collectionId, collectionName,
                            collectionCreated,
                            artifactId, artifactCreated, artifact);
                        continue;
                    }

                    ArtifactFactory factory = factoryLookup
                        .getArtifactFactory(factoryName);

                    if (factory == null) {
                        logger.error("factory '" + factoryName + "' not found");
                        continue;
                    }

                    byte [] bytes = result.getBytes("data");

                    artifact = factory.getSerializer().fromBytes(bytes);

                    if (artifact != null) {
                        alc.artifactLoaded(
                            userId,
                            collectionId, collectionName, collectionCreated,
                            artifactId, artifactCreated, artifact);
                    }

                    alreadyLoaded.put(artifactId, artifact);
                }
                return true;
            }
        }.runRead();

        if (logger.isDebugEnabled()) {
            logger.debug("loadAllArtifacts success: " + success);
        }

        return success;
    }

    @Override
    public void killedArtifacts(List<String> identifiers) {
        logger.debug("killedArtifacts");
        for (BackendListener listener: listeners) {
            listener.killedArtifacts(identifiers, this);
        }
    }

    @Override
    public void killedCollections(List<String> identifiers) {
        logger.debug("killedCollections");
        for (BackendListener listener: listeners) {
            listener.killedCollections(identifiers, this);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
