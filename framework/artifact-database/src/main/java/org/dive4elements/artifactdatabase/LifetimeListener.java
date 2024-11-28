package org.dive4elements.artifactdatabase;

import org.dive4elements.artifacts.GlobalContext;

import org.w3c.dom.Document;

public interface LifetimeListener
{
    void setup(Document document);

    void systemUp(GlobalContext globalContext);

    void systemDown(GlobalContext globalContext);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
