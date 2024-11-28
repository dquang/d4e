package org.dive4elements.artifactdatabase.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SQLExecutor
{
    private static Logger logger = LogManager.getLogger(SQLExecutor.class);

    public class Instance {

        public Connection        conn;
        public PreparedStatement stmnt;
        public ResultSet         result;

        public Instance() {
        }

        public void reset() throws SQLException {
            if (result != null) {
                result.close();
                result = null;
            }
            if (stmnt != null) {
                result = null;
                stmnt.close();
            }
        }

        public PreparedStatement prepareStatement(String query)
        throws SQLException {
            return stmnt = conn.prepareStatement(query);
        }

        public void close() {
            if (result != null) {
                try { result.close(); }
                catch (SQLException sqle) {}
            }
            if (stmnt != null) {
                try { stmnt.close(); }
                catch (SQLException sqle) {}
            }
            if (conn != null) {
                try { conn.close(); }
                catch (SQLException sqle) {}
            }
        }

        public boolean runWriteNoRollback() {
            DataSource dataSource = dbConnection.getDataSource();
            try {
                conn = dataSource.getConnection();
                try {
                    conn.setAutoCommit(false);
                    return doIt();
                }
                catch (SQLException sqle) {
                    throw sqle;
                }
            }
            catch (SQLException sqle) {
                logger.error(sqle.getLocalizedMessage(), sqle);
            }
            finally {
                close();
            }
            return false;
        }

        public boolean runWrite() {
            DataSource dataSource = dbConnection.getDataSource();
            try {
                conn = dataSource.getConnection();
                try {
                    conn.setAutoCommit(false);
                    return doIt();
                }
                catch (SQLException sqle) {
                    conn.rollback();
                    throw sqle;
                }
            }
            catch (SQLException sqle) {
                logger.error(sqle.getLocalizedMessage(), sqle);
            }
            finally {
                close();
            }
            return false;
        }

        public boolean runRead() {
            DataSource dataSource = dbConnection.getDataSource();
            try {
                conn = dataSource.getConnection();
                return doIt();
            }
            catch (SQLException sqle) {
                logger.error(sqle.getLocalizedMessage(), sqle);
            }
            finally {
                close();
            }
            return false;
        }

        public boolean doIt() throws SQLException {
            return true;
        }
    } // class Instance

    protected DBConnection           dbConnection;

    public SQLExecutor() {
    }

    public SQLExecutor(DBConnection dbConnection) {
        this();
        this.dbConnection = dbConnection;
    }

    public DBConnection getDBConnection() {
        return dbConnection;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
