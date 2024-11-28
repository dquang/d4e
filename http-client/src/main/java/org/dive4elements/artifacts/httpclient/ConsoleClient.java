/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts.httpclient;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.httpclient.http.HttpClient;
import org.dive4elements.artifacts.httpclient.http.HttpClientImpl;
import org.dive4elements.artifacts.httpclient.http.response.DocumentResponseHandler;

import org.dive4elements.artifacts.httpclient.exceptions.ConnectionException;
import org.dive4elements.artifacts.httpclient.exceptions.NoSuchOptionException;
import org.dive4elements.artifacts.httpclient.objects.Artifact;
import org.dive4elements.artifacts.httpclient.utils.ArtifactProtocolUtils;
import org.dive4elements.artifacts.httpclient.utils.Configuration;
import org.dive4elements.artifacts.httpclient.utils.XFormNamespaceContext;
import org.dive4elements.artifacts.httpclient.utils.XMLUtils;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ConsoleClient
{
    /**
     * The path of the configuration directory.
     */
    public static final String CONFIG_PATH = System.getProperty("config.dir",
                                                                "conf");

    public static final String CONFIG = System.getProperty("config.file",
                                                           "use_case1.conf");


    public static final String XPATH_DYNAMIC = "/art:result/art:ui/art:dynamic";

    /**
     * The logger used in this class.
     */
    private static Logger logger = LogManager.getLogger(ConsoleClient.class);


    public static final Configuration readConfiguration() {
        File configDir  = new File(CONFIG_PATH);
        File configFile = new File(configDir, CONFIG);

        logger.debug("Configuration file: " + configFile.getAbsolutePath());

        if (configFile.isFile() && configFile.canRead()) {
            try {
                Configuration conf = new Configuration(configFile);
                conf.initialize();

                return conf;
            }
            catch (IOException ioe) {
                logger.error("Error while reading configuration.");
            }
        }

        return null;
    }


    public static void main( String[] args )
    {
        logger.info("Starting console client.");

        Configuration conf = readConfiguration();

        String serverHost  = (String) conf.getServerSettings("host");
        String serverPort  = (String) conf.getServerSettings("port");
        HttpClient client   = new HttpClientImpl(serverHost + ":" + serverPort);

        try {
            Document create = ArtifactProtocolUtils.createCreateDocument(
                (String) conf.getArtifactSettings("fis"));
            Artifact artifact = (Artifact) client.create(create, null);

            Map attr       = new HashMap();
            String   product  = (String) conf.getArtifactSettings("product");
            String[] products = extractOptions(client, artifact, product);
            attr.put("product", products[0]);

            feedAndGo(client, artifact, attr, "timeSeries");

            attr.clear();
            String   area  = (String) conf.getArtifactSettings("areaid");
            String[] areas = extractOptions(client, artifact, area);
            attr.put("areaid", areas[0]);
            feedAndGo(client, artifact, attr, "timeseries_without_geom");

            attr.clear();
            String   feature  = (String) conf.getArtifactSettings("featureid");
            String[] features = extractOptions(client, artifact, feature);
            attr.put("featureid", features[0]);
            feedAndGo(client, artifact, attr, "timeseries_vector_scalar");

            attr.clear();
            String   vector  = (String) conf.getArtifactSettings("vectorscalar");
            String[] vectors = extractOptions(client, artifact, vector);
            attr.put("vectorscalar", vectors[0]);
            feedAndGo(client, artifact, attr, "timeseries_parameter");

            attr.clear();
            String   parameter  = (String) conf.getArtifactSettings("parameterid");
            String[] parameters = extractOptions(client, artifact, parameter);
            attr.put("parameterid", parameters);
            feedAndGo(client, artifact, attr, "timeseries_depth_height");

            attr.clear();
            String   measure  = (String) conf.getArtifactSettings("measurementid");
            String[] measures = extractMeasurements(client, artifact, measure);
            attr.put("measurementid", measures);
            feedAndGo(client, artifact, attr, "timeseries_interval");

            attr.clear();
            String min = (String) conf.getArtifactSettings("minvalue");
            String max = (String) conf.getArtifactSettings("maxvalue");
            attr.put("minvalue", min);
            attr.put("maxvalue", max);
            feedAndGo(client, artifact, attr, "timeseries_calculate_results");

            try {
                Map opts = new HashMap();
                opts.put("mime-type", conf.getOutputSettings("mime-type"));
                opts.put("width", conf.getOutputSettings("width"));
                opts.put("height", conf.getOutputSettings("height"));
                opts.put("points", conf.getOutputSettings("points"));

                Document chart =
                    ArtifactProtocolUtils.createChartDocument(artifact, opts);

                String dir = (String) conf.getOutputSettings("directory");

                File outDir     = new File(dir);
                File output     = new File(outDir, "output.png");
                OutputStream os = new FileOutputStream(output);

                client.out(artifact, chart, "chart", os);
            }
            catch (IOException ioe) {
                logger.error(
                    "IO error while writing the output: " + ioe.getMessage());
            }

            logger.debug("Finished console client.");
        }
        catch (ConnectionException ce) {
            logger.error(ce.getMessage());
        }
        catch (NoSuchOptionException nsoe) {
            logger.error(
                "No such option found: " + nsoe.getMessage());
        }
    }


    public static void feedAndGo(
        HttpClient client,
        Artifact  artifact,
        Map       attr,
        String target)
    throws ConnectionException
    {
        Document feed = ArtifactProtocolUtils.createFeedDocument(artifact, attr);
        client.feed(artifact, feed, new DocumentResponseHandler());

        Document advance = ArtifactProtocolUtils.createAdvanceDocument(
            artifact,
            target);

        client.advance(artifact, advance, new DocumentResponseHandler());
    }


    /**
     * XXX I think, this method should be implemented somewhere else to be able
     * to re-use this implementation. But this method needs more work to be more
     * abstract, so it needs to be reimplemented later, I think.
     */
    public static String[] extractOptions(
        HttpClient client,
        Artifact  artifact,
        String    text)
    throws NoSuchOptionException, ConnectionException
    {
        Document describe = ArtifactProtocolUtils.createDescribeDocument(
            artifact, true);

        Document description = (Document) client.describe(
            artifact, describe, new DocumentResponseHandler());

        List pieces  = Arrays.asList(text.split(","));
        List options = new ArrayList(pieces.size());

        Node dynamic   = XMLUtils.getNodeXPath(description, XPATH_DYNAMIC);

        // TODO We should handle these cases better!!
        NodeList items = (NodeList) XMLUtils.getXPath(
            dynamic, "xform:select1/xform:choices/xform:item",
            XPathConstants.NODESET, XFormNamespaceContext.INSTANCE);

        if (items == null || items.getLength() == 0) {
            items = (NodeList) XMLUtils.getXPath(
                dynamic, "xform:select/xform:choices/xform:item",
                XPathConstants.NODESET, XFormNamespaceContext.INSTANCE);
        }

        if (items == null || items.getLength() == 0) {
            items = (NodeList) XMLUtils.getXPath(
                dynamic, "xform:group/xform:select/xform:item",
                XPathConstants.NODESET, XFormNamespaceContext.INSTANCE);
        }


        for (int i = 0; i < items.getLength(); i++) {
            Node item  = items.item(i);
            Node label = (Node) XMLUtils.getXPath(
                item, "xform:label", XPathConstants.NODE,
                XFormNamespaceContext.INSTANCE);

            Node value = (Node) XMLUtils.getXPath(
                item, "xform:value", XPathConstants.NODE,
                XFormNamespaceContext.INSTANCE);

            if (pieces.indexOf(label.getTextContent()) >= 0)
                options.add(value.getTextContent());
        }

        if (options.isEmpty())
            throw new NoSuchOptionException(text);

        return (String[]) options.toArray(new String[options.size()]);
    }


    /**
     * XXX This method extracts the measurement ids depending on the user
     * configuration from describe document. Currently, this is a special case
     * that should be handled the same way as all the other options in the
     * describe document.
     */
    public static String[] extractMeasurements(
        HttpClient client,
        Artifact  artifact,
        String    text)
    throws NoSuchOptionException, ConnectionException
    {
        Document describe = ArtifactProtocolUtils.createDescribeDocument(
            artifact, true);

        Document description = (Document) client.describe(
            artifact, describe, new DocumentResponseHandler());

        List pieces  = Arrays.asList(text.split(","));
        List options = new ArrayList(pieces.size());

        Node dynamic = XMLUtils.getNodeXPath(description, XPATH_DYNAMIC);

        NodeList params = (NodeList) XMLUtils.getXPath(
            dynamic, "xform:group/xform:select",
            XPathConstants.NODESET, XFormNamespaceContext.INSTANCE);

        for (int i = 0; i < params.getLength(); i++) {
            Node param = params.item(i);

            NodeList items = (NodeList) XMLUtils.getXPath(
                param, "xform:item[@disabled='false']", XPathConstants.NODESET,
                XFormNamespaceContext.INSTANCE);

            for (int j = 0; j < items.getLength(); j++) {
                Node item = items.item(j);

                Node label = (Node) XMLUtils.getXPath(
                    item, "xform:label", XPathConstants.NODE,
                    XFormNamespaceContext.INSTANCE);

                if (pieces.indexOf(label.getTextContent()) < 0) {
                    continue;
                }

                Node value = (Node) XMLUtils.getXPath(
                    item, "xform:value", XPathConstants.NODE,
                    XFormNamespaceContext.INSTANCE);

                options.add(value.getTextContent());
            }
        }

        if (options.isEmpty())
            throw new NoSuchOptionException(text);

        return (String[]) options.toArray(new String[options.size()]);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8:
