/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.common.utils.Config;
import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifactdatabase.db.SQL;
import org.dive4elements.artifactdatabase.db.SQLExecutor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The database cleaner runs in background. It sleep for a configurable
 * while and when it wakes up it removes outdated artifacts from the
 * database. Outdated means that the the last access to the artifact
 * is longer aga then the time to live of this artifact.<br>
 * Before the artifact is finally removed from the system it is
 * revived one last time an the #endOfLife() method of the artifact
 * is called.<br>
 * The artifact implementations may e.g. use this to remove some extrenal
 * resources form the system.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DatabaseCleaner
extends      Thread
{
    /**
     * Implementors of this interface are able to create a
     * living artifact from a given byte array.
     */
    public interface ArtifactReviver {

        /**
         * Called to revive an artifact from a given byte array.
         * @param factoryName The name of the factory which
         * created this artifact.
         * @param bytes The bytes of the serialized artifact.
         * @return The revived artfiact.
         */
        Artifact reviveArtifact(String factoryName, byte [] bytes);

        void killedArtifacts(List<String> identifiers);
        void killedCollections(List<String> identifiers);

    } // interface ArtifactReviver

    public interface LockedIdsProvider {
        Set<Integer> getLockedIds();
    } // interface LockedIdsProvider

    private static Logger logger = LogManager.getLogger(DatabaseCleaner.class);

    /**
     * Number of artifacts to be loaded at once. Used to
     * mitigate the problem of a massive denial of service
     * if too many artifacts have died since last cleanup.
     */
    public static final int MAX_ROWS = 50;

    public static final Set<Integer> EMPTY_IDS = Collections.emptySet();

    /**
     * The SQL statement to select the outdated artifacts.
     */
    public String SQL_OUTDATED;

    public String SQL_OUTDATED_COLLECTIONS;
    public String SQL_DELETE_COLLECTION_ITEMS;
    public String SQL_DELETE_COLLECTION;
    public String SQL_COLLECTION_ITEMS_ARTIFACT_IDS;

    /**
     * The SQL statement to delete some artifacts from the database.
     */
    public String SQL_DELETE_ARTIFACT;

    /**
     * XPath to figure out how long the cleaner should sleep between
     * cleanups. This is stored in the global configuration.
     */
    public static final String SLEEP_XPATH =
        "/artifact-database/cleaner/sleep-time/text()";

    /**
     * Default nap time between cleanups: 5 minutes.
     */
    public static final long SLEEP_DEFAULT =
        5 * 60 * 1000L; // 5 minutes

    /**
     * The configured nap time.
     */
    protected long sleepTime;

    /**
     * Internal locking mechanism to prevent some race conditions.
     */
    protected Object sleepLock = new Object();

    /**
     * A reference to the global context.
     */
    protected Object context;

    /**
     * A specialized Id filter which only delete some artifacts.
     * This is used to prevent deletion of living artifacts.
     */
    protected LockedIdsProvider lockedIdsProvider;

    /**
     * The reviver used to bring the dead artifact on last
     * time back to live to call endOfLife() on them.
     */
    protected ArtifactReviver reviver;

    protected SQLExecutor sqlExecutor;

    /**
     * Default constructor.
     */
    public DatabaseCleaner() {
    }

    /**
     * Constructor to create a cleaner with a given global context
     * and a given reviver.
     * @param context The global context of the artifact database
     * @param reviver The reviver to awake artifact one last time.
     */
    public DatabaseCleaner(
            Object          context,
            ArtifactReviver reviver,
            SQLExecutor     sqlExecutor,
            DBConfig        config
    ) {
        setDaemon(true);
        sleepTime = getSleepTime();
        this.context = context;
        this.reviver = reviver;
        this.sqlExecutor = sqlExecutor;
        setupSQL(config.getSQL());
    }

    protected void setupSQL(SQL sql) {
        SQL_OUTDATED                      = sql.get("artifacts.outdated");
        SQL_OUTDATED_COLLECTIONS          = sql.get("collections.outdated");
        SQL_DELETE_COLLECTION_ITEMS       = sql.get("delete.collection.items");
        SQL_DELETE_COLLECTION             = sql.get("delete.collection");
        SQL_DELETE_ARTIFACT               = sql.get("artifacts.delete");
        SQL_COLLECTION_ITEMS_ARTIFACT_IDS = sql.get("collection.items.artifact.id");
    }

    /**
     * Sets the filter that prevents deletion of living artifacts.
     * Living artifacts are artifacts which are currently active
     * inside the artifact database. Deleting them in this state
     * would create severe internal problems.
     */
    public void setLockedIdsProvider(LockedIdsProvider lockedIdsProvider) {
        this.lockedIdsProvider = lockedIdsProvider;
    }

    /**
     * External hook to tell the cleaner to wake up before its
     * regular nap time is over. This is the case when the artifact
     * database finds an artifact which is already outdated.
     */
    public void wakeup() {
        synchronized (sleepLock) {
            sleepLock.notify();
        }
    }

    /**
     * Fetches the sleep time from the global configuration.
     * @return the time to sleep between database cleanups in ms.
     */
    protected static long getSleepTime() {
        String sleepTimeString = Config.getStringXPath(SLEEP_XPATH);

        if (sleepTimeString == null) {
            return SLEEP_DEFAULT;
        }
        try {
            // sleep at least one second
            return Math.max(Long.parseLong(sleepTimeString), 1000L);
        }
        catch (NumberFormatException nfe) {
            logger.warn("Cleaner sleep time defaults to " + SLEEP_DEFAULT);
        }
        return SLEEP_DEFAULT;
    }

    private static class IdIdentifier {

        int     id;
        String  identifier;

        private IdIdentifier(int id, String identifier) {
            this.id         = id;
            this.identifier = identifier;
        }
    } // class IdIdentifier

    private static final class IdData
    extends IdIdentifier
    {
        byte [] data;
        String  factoryName;

        public IdData(
            int     id,
            String  factoryName,
            byte [] data,
            String  identifier
        ) {
            super(id, identifier);
            this.factoryName = factoryName;
            this.data        = data;
        }
    } // class IdData

    /**
     * Cleaning is done in two phases. First we fetch a list of ids
     * of artifacts. If there are artifacts the cleaning is done.
     * Second we load the artifacts one by one one and call there
     * endOfLife() method. In this loop we remove them from database, too.
     * Each deletion is commited to ensure that a sudden failure
     * of the artifact database server does delete artifacts twice
     * or does not delete them at all. After this the first step
     * is repeated.
     */
    protected void cleanup() {
        logger.info("database cleanup");

        final Set<Integer> lockedIds = lockedIdsProvider != null
            ? lockedIdsProvider.getLockedIds()
            : EMPTY_IDS;

        final String questionMarks = lockedIds.isEmpty()
            ? "-666" // XXX: A bit hackish.
            : StringUtils.repeat('?', lockedIds.size(), ',');

        final List<String> deletedCollections = new ArrayList<String>();
        final List<String> deletedArtifacts   = new ArrayList<String>();

        SQLExecutor.Instance exec = sqlExecutor.new Instance() {

            @Override
            public boolean doIt() throws SQLException {

                PreparedStatement collectionItems = null;
                PreparedStatement fetchIds        = null;
                PreparedStatement stmnt           = null;
                ResultSet         result          = null;

                HashSet<Integer> collectionItemsIds =
                    new HashSet<Integer>();

                try {
                    collectionItems = conn.prepareStatement(
                        SQL_COLLECTION_ITEMS_ARTIFACT_IDS);

                    result = collectionItems.executeQuery();

                    while (result.next()) {
                        collectionItemsIds.add(result.getInt(1));
                    }
                    result.close(); result = null;

                    fetchIds = conn.prepareStatement(
                        SQL_OUTDATED.replace("$LOCKED_IDS$", questionMarks));

                    // Fetch ids of outdated collections
                    stmnt = conn.prepareStatement(
                        SQL_OUTDATED_COLLECTIONS.replace(
                            "$LOCKED_IDS$", questionMarks));

                    // fill in the locked ids
                    int idx = 1;
                    for (Integer id: lockedIds) {
                        fetchIds.setInt(idx, id);
                        stmnt   .setInt(idx, id);
                        ++idx;
                    }

                    ArrayList<IdIdentifier> cs = new ArrayList<IdIdentifier>();
                    result = stmnt.executeQuery();
                    while (result.next()) {
                        cs.add(new IdIdentifier(
                            result.getInt(1),
                            result.getString(2)));
                    }

                    result.close(); result = null;
                    stmnt.close();  stmnt  = null;

                    // delete collection items
                    stmnt = conn.prepareStatement(SQL_DELETE_COLLECTION_ITEMS);

                    for (IdIdentifier id: cs) {
                        logger.debug("Mark collection for deletion: " + id.id);
                        stmnt.setInt(1, id.id);
                        stmnt.execute();
                    }

                    stmnt.close(); stmnt = null;

                    // delete collections
                    stmnt = conn.prepareStatement(SQL_DELETE_COLLECTION);

                    for (IdIdentifier id: cs) {
                        stmnt.setInt(1, id.id);
                        stmnt.execute();
                        deletedCollections.add(id.identifier);
                    }

                    stmnt.close(); stmnt = null;
                    conn.commit();

                    cs = null;

                    // remove artifacts
                    stmnt = conn.prepareStatement(SQL_DELETE_ARTIFACT);

                    for (;;) {
                        List<IdData> ids = new ArrayList<IdData>();

                        result = fetchIds.executeQuery();

                        int total = 0;

                        while (result.next()) {
                            total++;
                            int id = result.getInt(1);
                            if (!collectionItemsIds.contains(id)) {
                                ids.add(new IdData(
                                    id,
                                    result.getString(2),
                                    result.getBytes(3),
                                    result.getString(4)));
                            }
                        }

                        result.close(); result = null;

                        if (total == 0) {
                            break;
                        }

                        if (ids.isEmpty()) {
                            break;
                        }

                        for (int i = ids.size()-1; i >= 0; --i) {
                            IdData idData = ids.get(i);
                            Artifact artifact = reviver.reviveArtifact(
                                idData.factoryName, idData.data);
                            idData.data = null;

                            logger.debug("Prepare Artifact (id="
                                + idData.id + ") for deletion.");

                            stmnt.setInt(1, idData.id);
                            stmnt.execute();
                            conn.commit();

                            try {
                                if (artifact != null) {
                                    logger.debug("Call endOfLife for Artifact: "
                                        + artifact.identifier());

                                    artifact.endOfLife(context);
                                }
                            }
                            catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }

                            deletedArtifacts.add(idData.identifier);
                        } // for all fetched data
                    }
                }
                finally {
                    if (result != null) {
                        try { result.close(); }
                        catch (SQLException sqle) {}
                    }
                    if (stmnt != null) {
                        try { stmnt.close(); }
                        catch (SQLException sqle) {}
                    }
                    if (fetchIds != null) {
                        try { fetchIds.close(); }
                        catch (SQLException sqle) {}
                    }
                    if (collectionItems != null) {
                        try { collectionItems.close(); }
                        catch (SQLException sqle) {}
                    }
                }
                return true;
            }
        };

        if (!exec.runWriteNoRollback()) {
            logger.error("Deleting artifacts failed.");
        }

        if (!deletedCollections.isEmpty()) {
            reviver.killedCollections(deletedCollections);
        }

        if (!deletedArtifacts.isEmpty()) {
            reviver.killedArtifacts(deletedArtifacts);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                "collections removed: " + deletedCollections.size());
            logger.debug(
                "artifacts removed: " + deletedArtifacts.size());
        }
    }

    /**
     * The main code of the cleaner. It sleeps for the configured
     * nap time, cleans up the database, sleeps again and so on.
     */
    @Override
    public void run() {
        logger.info("sleep time: " + sleepTime + "ms");
        for (;;) {
            cleanup();
            long startTime = System.currentTimeMillis();

            try {
                synchronized (sleepLock) {
                    sleepLock.wait(sleepTime);
                }
            }
            catch (InterruptedException ie) {
            }

            long stopTime = System.currentTimeMillis();

            if (logger.isDebugEnabled()) {
                logger.debug("Cleaner slept " + (stopTime - startTime) + "ms");
            }
        } // for (;;)
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
