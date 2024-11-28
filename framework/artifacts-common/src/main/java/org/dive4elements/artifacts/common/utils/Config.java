/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.common.utils;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

/**
 * The central access to the configuration of the artifact database.
 * This class provides some static methods to access the central
 * configuration XML file via XPath.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public final class Config
{
    private static Logger logger = LogManager.getLogger(Config.class);

    /**
     * System property name where to find the configuration directory.
     */
    public static final String CONFIG_DIR = "artifact.database.dir";

    /**
     * Default path to the configuration directory if none
     * was specified by the CONFIG_DIR system property.
     */
    public static final File CONFIG_DIR_DEFAULT =
        new File(new File(System.getProperty("user.home",
            System.getProperty("user.dir", "."))), ".artifactdb");

    /**
     * Name of the central configuration XML file.
     */
    public static final String CONFIG_FILE  = "conf.xml";

    /**
     * Name of the configuration filename alias to be use
     * within the configuration. This alias is replaced by
     * the real path.
     */
    public static final String CONFIG_DIR_PLACEHOLDER =
        "${artifacts.config.dir}";

    private static Document config;

    private Config() {
    }

    /**
     * Singleton access to the central XML configuration document.
     * @return The central XML configuration document.
     */
    public static synchronized final Document getConfig() {
        if (config == null) {
            config = loadConfig();
        }
        return config;
    }

    /**
     * Returns the path to the configuartion directory. If a path
     * was specified via the CONFIG_DIR system property this one
     * is used. Else it falls back to default configuration path.
     * @return The path to the configuartion directory.
     */
    public static File getConfigDirectory() {
        String configDirString = System.getProperty(CONFIG_DIR);

        File configDir = configDirString != null
            ? new File(configDirString)
            : CONFIG_DIR_DEFAULT;

        if (!configDir.isDirectory()) {
            logger.warn("'" + configDir + "' is not a directory.");
            configDir = CONFIG_DIR_DEFAULT;
        }

        return configDir;
    }

    /**
     * Replaces the CONFIG_DIR_PLACEHOLDER alias with the real path
     * of the configuration directory.
     * @param path The path containing the CONFIG_DIR_PLACEHOLDER placeholder.
     * @return The path where the CONFIG_DIR_PLACEHOLDER placeholders are
     * replaced by the real path name.
     */
    public static String replaceConfigDir(String path) {
        String configDir = getConfigDirectory().getAbsolutePath();
        return path.replace(CONFIG_DIR_PLACEHOLDER, configDir);
    }

    private static Document loadConfig() {

        File configDir = getConfigDirectory();

        File file = new File(configDir, CONFIG_FILE);

        if (!file.canRead() && !file.isFile()) {
            logger.error("Cannot read config file '"
                + file + "'.");
            return null;
        }

        try {
            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            factory.setValidating(false); // XXX: This may change in future.
            return factory.newDocumentBuilder().parse(file);
        }
        catch (SAXException se) {
            logger.error(se.getLocalizedMessage(), se);
        }
        catch (ParserConfigurationException pce) {
            logger.error(pce.getLocalizedMessage(), pce);
        }
        catch (IOException ioe) {
            logger.error(ioe.getLocalizedMessage());
        }

        return null;
    }

    /**
     * Convenience method to search within a given document tree via XPath.
     * See {@link XMLUtils#xpath(Object, String, QName) } for details.
     * @param root The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query.
     * @param returnType The type of the result.
     * @return The result of type 'returnTyp' or null if something went
     * wrong during XPath evaluation.
     */
    public static final Object getXPath(
        Object root, String query, QName returnType
    ) {
        return XMLUtils.xpath(root, query, returnType);
    }

    /**
     * Convenience method to search within the central configuration via XPath.
     * See {@link XMLUtils#xpath(Object, String, QName) } for details.
     * @param query The XPath query.
     * @param returnType The type of the result.
     * @return The result of type 'returnTyp' or null if something went
     * wrong during XPath evaluation.
     */
    public static final Object getXPath(String query, QName returnType) {
        return XMLUtils.xpath(getConfig(), query, returnType);
    }

    /**
     * Convenience method to search for a node list within the central
     * configuation document via XPath.
     * @param query The XPath query.
     * @return The queried node list or null if something went
     * wrong during XPath evaluation.
     */
    public static final NodeList getNodeSetXPath(String query) {
        return (NodeList)getXPath(query, XPathConstants.NODESET);
    }

    /**
     * Convenience method to search for a node within the central
     * configuation document via XPath.
     * @param query The XPath query.
     * @return The queried node or null if something went
     * wrong during XPath evaluation.
     */
    public static final Node getNodeXPath(String query) {
        return (Node)getXPath(query, XPathConstants.NODE);
    }

    /**
     * Convenience method to search for a string within the central
     * configuation document via XPath.
     * @param xpath The XPath query.
     * @return The queried string or null if something went
     * wrong during XPath evaluation.
     */
    public static final String getStringXPath(String xpath) {
        return getStringXPath(xpath, null);
    }

    /**
     * Convenience method to search for a string within the central
     * configuation document via XPath.
     * @param query The XPath query.
     * @param def The string to be returned if the search has no results.
     * @return The queried string or the default value if something went
     * wrong during XPath evaluation.
     */
    public static final String getStringXPath(String query, String def) {
        String s = (String)getXPath(query, XPathConstants.STRING);
        return s == null || s.length() == 0
            ? def
            : s;
    }

    /**
     * Convenience method to search for a node list within a given tree
     * via XPath.
     * @param root The root of the tree to be searched in.
     * @param query The XPath query.
     * @return The queried node list or null if something went
     * wrong during XPath evaluation.
     */
    public static final NodeList getNodeSetXPath(Object root, String query) {
        return (NodeList)getXPath(root, query, XPathConstants.NODESET);
    }

    /**
     * Convenience method to search for a node within a given tree
     * via XPath.
     * @param root The root of the tree to be searched in.
     * @param query The XPath query.
     * @return The queried node or null if something went
     * wrong during XPath evaluation.
     */
    public static final Node getNodeXPath(Object root, String query) {
        return (Node)getXPath(root, query, XPathConstants.NODE);
    }

    /**
     * Convenience method to search for a string within a given tree
     * via XPath.
     * @param root The root of the tree to be searched in.
     * @param xpath The XPath query.
     * @return The queried string or null if something went
     * wrong during XPath evaluation.
     */
    public static final String getStringXPath(Object root, String xpath) {
        return getStringXPath(root, xpath, null);
    }

    /**
     * Convenience method to search for a string within a given tree
     * via XPath.
     * @param root The root of the tree to be searched in.
     * @param query xpath The XPath query.
     * @param def The string to be returned if the search has no results.
     * @return The queried string or the default value if something went
     * wrong during XPath evaluation.
     */
    public static final String getStringXPath(
        Object root, String query, String def
    ) {
        String s = (String)getXPath(root, query, XPathConstants.STRING);
        return s == null || s.length() == 0
            ? def
            : s;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
