package org.dive4elements.artifactdatabase;

import java.util.List;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactCollection;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.artifacts.User;

import org.w3c.dom.Document;

public interface BackendListener
{
    void setup(GlobalContext globalContext);

    void createdArtifact(Artifact artifact, Backend backend);

    void storedArtifact(Artifact artifact, Backend backend);

    void createdUser(User user, Backend backend);

    void deletedUser(String identifier, Backend backend);

    void createdCollection(ArtifactCollection collection, Backend backend);

    void deletedCollection(String identifier, Backend backend);

    void changedCollectionAttribute(
        String   identifier,
        Document document,
        Backend  backend);

    void changedCollectionItemAttribute(
        String   collectionId,
        String   artifactId,
        Document document,
        Backend  backend);

    void addedArtifactToCollection(
        String  artifactId,
        String  collectionId,
        Backend backend);

    void removedArtifactFromCollection(
        String  artifactId,
        String  collectionId,
        Backend backend);

    void setCollectionName(
        String collectionId,
        String name);

    void killedCollections(
        List<String> identifiers,
        Backend      backend);

    void killedArtifacts(
        List<String> identifiers,
        Backend      backend);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
