package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.common.utils.Config;

import org.dive4elements.artifactdatabase.db.SQL;
import org.dive4elements.artifactdatabase.db.DBConnection;

public class DBConfig
{
    /**
     * XPath to access the database driver within the global configuration.
     */
    public static final String DB_DRIVER =
        "/artifact-database/database/driver/text()";
    /**
     * XPath to access the database URL within the global configuration.
     */
    public static final String DB_URL =
        "/artifact-database/database/url/text()";
    /**
     * XPath to access the database use within the global configuration.
     */
    public static final String DB_USER =
        "/artifact-database/database/user/text()";
    /**
     * XPath to access the database password within the global configuration.
     */
    public static final String DB_PASSWORD =
        "/artifact-database/database/password/text()";

    private static DBConfig instance;

    private DBConnection dbConnection;
    private SQL          sql;

    private DBConfig() {
    }

    private DBConfig(DBConnection dbConnection, SQL sql) {
        this.dbConnection = dbConnection;
        this.sql          = sql;
    }

    public static synchronized DBConfig getInstance() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    public SQL getSQL() {
        return sql;
    }

    public DBConnection getDBConnection() {
        return dbConnection;
    }

    private static DBConfig createInstance() {

        String driver = Config.getStringXPath(
            DB_DRIVER, DBConnection.DEFAULT_DRIVER);

        String url = Config.getStringXPath(
            DB_URL, DBConnection.DEFAULT_URL);

        url = Config.replaceConfigDir(url);

        String user = Config.getStringXPath(
            DB_USER, DBConnection.DEFAULT_USER);

        String password = Config.getStringXPath(
            DB_PASSWORD, DBConnection.DEFAULT_PASSWORD);

        DBConnection dbConnection = new DBConnection(
            driver, url, user, password);

        SQL sql = new SQL(driver);

        return new DBConfig(dbConnection, sql);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
