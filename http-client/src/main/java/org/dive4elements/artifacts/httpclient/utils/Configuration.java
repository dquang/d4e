/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */
package org.dive4elements.artifacts.httpclient.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Configuration {

    private static final Logger logger = LogManager.getLogger(Configuration.class);

    private File config;

    private Map serverSettings;
    private Map artifactSettings;
    private Map outputSettings;

    public static final String XPATH_SERVER = "/configuration/artifact-server";
    public static final String XPATH_ARTIFACT = "/configuration/artifact";
    public static final String XPATH_OUTPUT = "/configuration/output";


    public Configuration(File config) {
        this.config      = config;
        serverSettings   = new HashMap();
        artifactSettings = new HashMap();
        outputSettings   = new HashMap();
    }


    public void initialize()
    throws IOException
    {
        Document conf = XMLUtils.readDocument(new FileInputStream(config));

        if (conf == null) {
            throw new IOException("Can't read config: " + config.getName());
        }

        readServerSettings(conf);
        readArtifactSettings(conf);
        readOutputSettings(conf);
    }


    private void readServerSettings(Document document)
    throws IOException
    {
        Node serverNode = XMLUtils.getNodeXPath(document, XPATH_SERVER);

        if (serverNode == null) {
            throw new IOException("No server configuration found.");
        }

        serverSettings = extractSettings(serverSettings, serverNode);
    }


    private void readArtifactSettings(Document document)
    throws IOException
    {
        Node artifactNode = XMLUtils.getNodeXPath(document, XPATH_ARTIFACT);

        if (artifactNode == null) {
            throw new IOException("No artifact configuration found.");
        }

        artifactSettings = extractSettings(artifactSettings, artifactNode);
    }


    private void readOutputSettings(Document document)
    throws IOException
    {
        Node outputNode = XMLUtils.getNodeXPath(document, XPATH_OUTPUT);

        if (outputNode == null) {
            throw new IOException("No output configuration found.");
        }

        outputSettings = extractSettings(outputSettings, outputNode);
    }


    private Map extractSettings(Map settings, Node node) {
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE)
                logger.debug(child.getNodeName() + "|" + child.getTextContent());
                settings.put(child.getNodeName(), child.getTextContent());
        }

        return settings;
    }


    public Object getServerSettings(String key) {
        return serverSettings.get(key);
    }


    public Object getArtifactSettings(String key) {
        return artifactSettings.get(key);
    }


    public Object getOutputSettings(String key) {
        return outputSettings.get(key);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
