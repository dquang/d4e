package org.dive4elements.artifactdatabase.rest;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.ArtifactDatabase;

public interface HTTPServer
{
    void setup(Document document);

    void startAsServer(ArtifactDatabase database);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
