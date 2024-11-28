package org.dive4elements.artifactdatabase.h2;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import org.dive4elements.artifactdatabase.DBConfig;

import org.dive4elements.artifactdatabase.db.SQL;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CollectionAccessUpdateTrigger
implements   Trigger
{
    private static Logger logger =
        LogManager.getLogger(CollectionAccessUpdateTrigger.class);

    public String COLLECTIONS_TOUCH_TRIGGER_FUNCTION;

    public void init(
        Connection conn,
        String     schemaName,
        String     triggerName,
        String     tableName,
        boolean    before,
        int        type
    )
    throws SQLException {
        logger.debug("CollectionAccessUpdateTrigger.init");
        setupSQL(DBConfig.getInstance().getSQL());
    }

    protected void setupSQL(SQL sql) {
        COLLECTIONS_TOUCH_TRIGGER_FUNCTION =
            sql.get("collections.touch.trigger.function");
    }

    public void fire(
        Connection conn,
        Object []  oldRow,
        Object []  newRow
    )
    throws SQLException {
        logger.debug("CollectionAccessUpdateTrigger.fire");
        PreparedStatement stmnt = conn.prepareStatement(
            COLLECTIONS_TOUCH_TRIGGER_FUNCTION);
        stmnt.setObject(1, newRow[0]);
        stmnt.execute();
        stmnt.close();
    }

    public void close() throws SQLException {
        logger.debug("CollectionAccessUpdateTrigger.close");
    }

    public void remove() throws SQLException {
        logger.debug("CollectionAccessUpdateTrigger.remove");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
