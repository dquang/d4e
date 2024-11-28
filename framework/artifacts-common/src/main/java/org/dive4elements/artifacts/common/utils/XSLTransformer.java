package org.dive4elements.artifacts.common.utils;

import java.io.InputStream;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Node;

public class XSLTransformer {

    private static Logger log = LogManager.getLogger(XSLTransformer.class);

    protected Map<String, Object> parameters;

    public XSLTransformer() {
    }

    public String transform(Node source, InputStream transform) {

        try {
            Source templateSource = new StreamSource(transform);
            TransformerFactory xformFactory =
                TransformerFactory.newInstance();
            Transformer transformer =
                xformFactory.newTransformer(templateSource);

            if (parameters != null) {
                for (Map.Entry<String, Object> entry: parameters.entrySet()) {
                    transformer.setParameter(entry.getKey(), entry.getValue());
                }
            }

            StringWriter result = new StringWriter();

            DOMSource    src = new DOMSource(source);
            StreamResult dst = new StreamResult(result);
            transformer.transform(src, dst);

            return result.toString();
        }
        catch (TransformerConfigurationException tce) {
            log.error(tce, tce);
        }
        catch (TransformerException te) {
            log.error(te, te);
        }

        return null;
    }

    public void addParameter(String key, Object value) {
        if (parameters == null) {
            parameters = new HashMap<String, Object>();
        }
        parameters.put(key, value);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
