package org.dive4elements.artifactdatabase.db;

import java.util.Properties;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SQL {

    private static Logger logger = LogManager.getLogger(SQL.class);

    protected Properties statements;

    public SQL() {
    }

    public SQL(String driver) {
        this(SQL.class, driver);
    }

    public SQL(Class clazz, String driver) {
        this(clazz, "/sql", driver);
    }

    public SQL(Class clazz, String resourcePath, String driver) {
        statements = loadStatements(clazz, resourcePath, driver);
    }

    public static final String driverToProperties(String driver) {
        return driver.replace('.', '-').toLowerCase() + ".properties";
    }

    /**
     * Returns key/value pairs of SQL statements for the used database
     * backend.
     * The concrete set of SQL statements is determined by the
     * used JDBC database driver which is configured in conf.xml.
     * The class name of the driver is transformed by replacing
     * all '.' with '_' and lower case the resulting string.
     * The transformed string is used to load a properties file
     * in '/sql/' which should contain the statements.
     * Example:<br>
     * <code>org.postgresql.Driver</code> results in loading of
     * <code>/sql/org-postgresql-driver.properties</code>.
     * @return The key/value pairs of SQL statements.
     */
    protected Properties loadStatements(
        Class  clazz,
        String resourcePath,
        String driver
    ) {
        logger.debug("loadStatements");

        Properties properties = new Properties();

        String resDriver = driverToProperties(driver);

        InputStream in = null;
        try {
            String res = resourcePath + "/" + resDriver;

            in = clazz.getResourceAsStream(res);

            if (in == null) {
                logger.warn("No SQL file for driver '" + driver + "' found.");
                resDriver = driverToProperties(DBConnection.DEFAULT_DRIVER);
                res = resourcePath + "/" + resDriver;

                in = clazz.getResourceAsStream(res);
                if (in == null) {
                    logger.error("No SQL file for driver '" +
                        DBConnection.DEFAULT_DRIVER + "' found.");
                }
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("found resource: " + res);
                }
            }

            if (in != null) {
                properties.load(in);
            }
        }
        catch (IOException ioe) {
            logger.error(ioe);
        }

        return properties;
    }

    public String get(String key) {
        boolean debug = logger.isDebugEnabled();
        if (debug) {
            logger.debug("looking for SQL " + key);
            logger.debug("statements != null: " + (statements != null));
        }

        String sql = statements.getProperty(key);

        if (sql == null) {
            logger.error("cannot find SQL for key '" + key + "'");
        }

        if (debug) {
            logger.debug("-> '" + sql + "'");
        }

        return sql;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
