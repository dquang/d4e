package org.dive4elements.artifactdatabase;

import java.util.List;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.User;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class DefaultBackendListener
implements   BackendListener
{
    private static Logger log = LogManager.getLogger(DefaultBackendListener.class);

    public DefaultBackendListener() {
    }

    @Override
    public void setup(GlobalContext globalContext) {
        log.debug("setup");
    }

    @Override
    public void createdArtifact(Artifact artifact, Backend backend) {
        log.debug("createdArtifact");
    }

    @Override
    public void storedArtifact(Artifact artifact, Backend backend) {
        log.debug("storedArtifact");
    }

    @Override
    public void createdUser(User user, Backend backend) {
        log.debug("createdUser");
    }

    @Override
    public void deletedUser(String identifier, Backend backend) {
        log.debug("deletedUser");
    }

    @Override
    public void createdCollection(
        ArtifactCollection collection,
        Backend            backend
    ) {
        log.debug("createdCollection");
    }

    @Override
    public void deletedCollection(String identifier, Backend backend) {
        log.debug("deletedCollection");
    }

    @Override
    public void changedCollectionAttribute(
        String   identifier,
        Document document,
        Backend  backend
    ) {
        log.debug("changedCollectionAttribute");
    }

    @Override
    public void changedCollectionItemAttribute(
        String   collectionId,
        String   artifactId,
        Document document,
        Backend  backend
    ) {
        log.debug("changedCollectionItemAttribute");
    }

    @Override
    public void addedArtifactToCollection(
        String  artifactId,
        String  collectionId,
        Backend backend
    ) {
        log.debug("addedArtifactToCollection");
    }

    @Override
    public void removedArtifactFromCollection(
        String  artifactId,
        String  collectionId,
        Backend backend
    ) {
        log.debug("removedArtifactFromCollection");
    }

    @Override
    public void setCollectionName(
        String collectionId,
        String name
    ) {
        log.debug("setCollectionName");
    }

    @Override
    public void killedCollections(List<String> identifiers, Backend backend) {
        log.debug("killedCollections");
    }

    @Override
    public void killedArtifacts(List<String> identifiers, Backend backend) {
        log.debug("killedArtifacts");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

