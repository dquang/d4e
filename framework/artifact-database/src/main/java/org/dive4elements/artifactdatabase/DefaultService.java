/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.Service;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.ServiceFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

/**
 * Trivial implementation of an artifact database service. Useful to
 * be subclassed.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public class DefaultService
implements   Service
{
    private static Logger logger = LogManager.getLogger(DefaultService.class);

    public static class Output implements Service.Output {

        protected Object data;
        protected String mimeType;

        public Output() {
        }

        public Output(Object data, String mimeType) {
            this.data     = data;
            this.mimeType = mimeType;
        }

        @Override
        public Object getData() {
            return data;
        }

        @Override
        public String getMIMEType() {
            return mimeType;
        }
    } // class Output

    @Override
    public Service.Output process(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        logger.debug("Service.process");
        return new Output(new byte[0], "application/octet-stream");
    }

    @Override
    public void setup(ServiceFactory factory, GlobalContext globalContext) {
        logger.debug("Service.setup");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
