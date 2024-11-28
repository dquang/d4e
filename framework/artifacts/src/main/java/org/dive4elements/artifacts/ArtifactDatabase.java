/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;

import java.util.Date;

/**
 * Interface of an artifact managing database.
 *
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface ArtifactDatabase
{
    /**
     * Implementations of this class defer the out call.
     */
    public interface DeferredOutput {

        /**
         * Inside this method the Artifact.out() method is called
         * with the given Outputstream.
         * @param output The stream to write the out() output into.
         * @throws IOException Thrown if an exception occurs while writing to
         * the output stream.
         */
        void write(OutputStream output) throws IOException;

    } // interface DeferredOut

    /**
     * List of artifact factories names accessible through the database.
     * @return pairs of names and descriptions of the factories.
     */
    String [][] artifactFactoryNamesAndDescriptions();

    /**
     * The methods returns a 'pure' factory which is not bound to
     * the artifact database. This means when an artifact is created
     * with the factory the created artifact is  not stored in the
     * artifact database.
     * @param factoryName The name of the queried artifact factory.
     * @return The queried artifact factory or null if corresponing
     * factory is found.
     */
    ArtifactFactory getInternalArtifactFactory(String factoryName);

    /**
     * Used to create an artifact with the factory which given
     * by the name 'factory'. The artifact is stored inside the
     * artifact database. If the creation succeeds the describe
     * document of the artifact is returned.
     * @param factory The name of the factory to create the artifact.
     * @param callMeta The meta information (languages et. al.) of the
     * creation.
     * @param data Optional input data to parameterize the creation.
     * @return The describe document of new artifact.
     * @throws ArtifactDatabaseException Thrown if something went wrong
     * during artifact creation.
     */
    Document createArtifactWithFactory(
        String   factory,
        CallMeta callMeta,
        Document data
    ) throws ArtifactDatabaseException;

    /**
     * Used to retrieve an artifact.<b>NOTE: artifact modifications are not
     * persisted to database!</b>
     */
    Artifact getRawArtifact(String identifier)
    throws ArtifactDatabaseException;

    /**
     * Returns the describe document of artifact identified
     * with the string 'artifact'.
     * @param artifact The identifier of the artifact.
     * @param data Optional input data to parameterize the description.
     * @param callMeta the meta information (language et. al.) of
     * the description.
     * @return The describe document of the artifact.
     * @throws ArtifactDatabaseException Thrown id something went wrong
     * during the creation of the describe document.
     */
    Document describe(String artifact, Document data, CallMeta callMeta)
        throws ArtifactDatabaseException;

    /**
     * Advances the artifact identified by 'artifact' to the state
     * 'target'. The result of the attempt is returned.
     * @param artifact The identifier of the artifact.
     * @param target The target state of the advance attempt.
     * @param callMeta The meta information (language et. al.) of the
     * advance attempt.
     * @return The result document of the advance attempt.
     * @throws ArtifactDatabaseException Thrown if something went wrong
     * during the advance attempt.
     */
    Document advance(String artifact, Document target, CallMeta callMeta)
        throws ArtifactDatabaseException;

    /**
     * Feeds the artifact identified by 'artifact' with some data 'data'.
     * @param artifact The identifier of the artifact.
     * @param data The data to be fed into the artifact.
     * @param callMeta The meta information (language et. al.) of the feed
     * attempt.
     * @return The result of the feed attempt.
     * @throws ArtifactDatabaseException Throw if something went wrong during
     * the feed attempt.
     */
    Document feed(String artifact, Document data, CallMeta callMeta)
        throws ArtifactDatabaseException;

    /**
     * Produces output for a given artifact identified by 'artifact' in
     * a requested format 'format'. The writing of the data is done when
     * the write() method of the returned DeferredOutput is called. This
     * optimizes the out streaming of the data because the call can be
     * deferred into to the calling context.
     * @param artifact The identifier of the artifact.
     * @param format The request format of the output.
     * @param callMeta The meta information (language et. al.) of the output.
     * @return The deferred output to be written later in the calling context.
     * @throws ArtifactDatabaseException Thrown if something went wrong during
     * producing the output.
     */
    DeferredOutput out(String artifact, Document format, CallMeta callMeta)
        throws ArtifactDatabaseException;


    /**
     * Produces output for a given artifact identified by 'artifact' in
     * a requested format 'format'. The writing of the data is done when
     * the write() method of the returned DeferredOutput is called. This
     * optimizes the out streaming of the data because the call can be
     * deferred into to the calling context.
     * @param artifact The identifier of the artifact.
     * @param format The request format of the output.
     * @param callMeta The meta information (language et. al.) of the output.
     * @return The deferred output to be written later in the calling context.
     * @throws ArtifactDatabaseException Thrown if something went wrong during
     * producing the output.
     */
    DeferredOutput out(
        String   artifact,
        String   type,
        Document format,
        CallMeta callMeta)
    throws ArtifactDatabaseException;

    /**
     * Produces an extenal represention of the artifact identified by
     * 'artifact' to be re-imported by #importArtifact(Document, CallMeta)
     * later.
     * @param artifact The identifier of the artifact.
     * @param callMeta The meta informatio (language et. al.) of the export.
     * @return A extenal representation of the artifact.
     * @throws ArtifactDatabaseException Thrown if something went wrong
     * during export.
     */
    Document exportArtifact(String artifact, CallMeta callMeta)
        throws ArtifactDatabaseException;

    /**
     * The symmetrical counter part of #exportArtifact(String, CallMeta).
     * It attempts to import the artifact which is coded inside the 'data'
     * document. When the import succeeds the new artifact is given a new
     * internal identifier and the describe document of the artifact is
     * returned.
     * @param data The encoded artifact. Has to be the output of
     * #exportArtifact(String, CallMeta).
     * @param callMeta The meta information (language et. al.) of the
     * import.
     * @return The describe document of the imported artifact.
     * @throws ArtifactDatabaseException Thrown if something went wrong during
     * the import attempt.
     */
    Document importArtifact(Document data, CallMeta callMeta)
        throws ArtifactDatabaseException;

    /**
     * Returns a list of services offered by this artifact database.
     * @return The array returned contains tuples of (name, description)
     * strings.
     */
    String [][] serviceNamesAndDescriptions();

    /**
     * Calls a service identified by 'service' with input document 'input'
     * to produce some output document.
     * @param service The name of the service.
     * @param input The input document.
     * @param callMeta The meta information (language et. al.) of the
     * service call.
     * @return The result document produced by the service.
     * @throws ArtifactDatabaseException Thrown if someting went wrong during
     * the service processing.
     */
    Service.Output process(String service, Document input, CallMeta callMeta)
        throws ArtifactDatabaseException;

    // User API

    Document listUsers(CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document findUser(Document data, CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document createUser(Document data, CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document deleteUser(String userId, CallMeta callMeta)
        throws ArtifactDatabaseException;

    // Collection API

    Document getCollectionsMasterArtifact(String collectionId, CallMeta meta)
        throws ArtifactDatabaseException;

    Document listCollections(String userId, CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document createCollection(String ownerId, Document data,
        CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document deleteCollection(String collectionId, CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document describeCollection(String collectionId, CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document getCollectionAttribute(String collectionId, CallMeta callMeta)
    throws ArtifactDatabaseException;

    Document setCollectionAttribute(
        String   collectionId,
        CallMeta callMeta,
        Document attribute)
    throws ArtifactDatabaseException;

    Document getCollectionItemAttribute(String collectionId, String artifactId,
        CallMeta callMeta) throws ArtifactDatabaseException;

    Document setCollectionItemAttribute(String collectionId, String artifactId,
        Document attribute, CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document addCollectionArtifact(String collectionId, String artifactId,
        Document input, CallMeta callMeta) throws ArtifactDatabaseException;

    Document removeCollectionArtifact(String collectionId, String artifactId,
        CallMeta callMeta) throws ArtifactDatabaseException;

    Document listCollectionArtifacts(String collectionId,
        CallMeta callMeta) throws ArtifactDatabaseException;

    DeferredOutput outCollection(String collectionId,
        Document format, CallMeta callMeta)
        throws ArtifactDatabaseException;

    DeferredOutput outCollection(String collectionId, String type,
        Document format, CallMeta callMeta)
        throws ArtifactDatabaseException;

    Document setCollectionTTL(String collectionId, Document doc, CallMeta meta)
    throws ArtifactDatabaseException;

    Document setCollectionName(String collectionId, Document doc, CallMeta meta)
    throws ArtifactDatabaseException;

    public interface ArtifactLoadedCallback {
        void artifactLoaded(
            String   userId,
            String   collectionId,
            String   collectionName,
            Date     collectionCreated,
            String   artifactId,
            Date     artifactCreated,
            Artifact artifact);
    }

    public void loadAllArtifacts(ArtifactLoadedCallback callback)
        throws ArtifactDatabaseException;

    String findArtifactUser(String artifactIdentifier);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
