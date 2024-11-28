package org.dive4elements.artifacts;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


public interface Hook {

    void setup(Node config);

    void execute(Artifact artifact, CallContext context, Document document);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
