package org.dive4elements.artifactdatabase.db;

import javax.sql.DataSource;

import java.io.File;

import org.apache.commons.pool.ObjectPool;

import org.apache.commons.pool.impl.GenericObjectPool;

import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;

import org.dive4elements.artifacts.common.utils.Config;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DBConnection
{
    private static Logger log = LogManager.getLogger(DBConnection.class);

    public static final String DEFAULT_DRIVER        = "org.h2.Driver";
    public static final String DEFAULT_USER          = "";
    public static final String DEFAULT_PASSWORD      = "";
    public static final String DEFAULT_DATABASE_FILE = "artifacts.db";
    public static final String DEFAULT_URL           = getDefaultURL();

    public static final String getDefaultURL() {
        File configDir = Config.getConfigDirectory();
        File databaseFile = new File(configDir, DEFAULT_DATABASE_FILE);
        return "jdbc:h2:" + databaseFile;
    }

    protected DataSource dataSource;

    protected String driver;
    protected String url;
    protected String user;
    protected String password;

    public DBConnection() {
    }

    public DBConnection(
        String driver,
        String url,
        String user,
        String password
    ) {
        this.driver   = driver;
        this.url      = url;
        this.user     = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public synchronized DataSource getDataSource() {
        if (dataSource == null) {
            if (log.isDebugEnabled()) {
                log.debug("create new datasource:");
                log.debug(" driver: " + driver);
                log.debug(" url   : " + url);
                log.debug(" user  : " + user);
            }

            try {
                synchronized (DBConnection.class) {
                    Class.forName(driver);
                }
            }
            catch (ClassNotFoundException cnfe) {
                log.error("cannot load driver", cnfe);
                return null;
            }

            DriverManagerConnectionFactory dmcf =
                new DriverManagerConnectionFactory(url, user, password);

            ObjectPool cp = new GenericObjectPool();

            PoolableConnectionFactory pcf = new PoolableConnectionFactory(
                dmcf, cp, null, null, false, false);

            dataSource = new PoolingDataSource(cp);
        }
        return dataSource;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
