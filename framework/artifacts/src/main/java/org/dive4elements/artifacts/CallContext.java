/*
 * Copyright (c) 2010 by Intevation GmbH
 *
 * This program is free software under the LGPL (>=v2.1)
 * Read the file LGPL.txt coming with the software for details
 * or visit http://www.gnu.org/licenses/ if it does not exist.
 */

package org.dive4elements.artifacts;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Instances of this interface are given to feed(), advance(), describe()
 * and out() to enable the artifact to communicate with the runtime system.
 * @author <a href="mailto:sascha.teichmann@intevation.de">Sascha L. Teichmann</a>
 */
public interface CallContext
{
    interface Listener {
        void setup(Document config, Node listenerNode);
        void init(CallContext callContext);
        void close(CallContext callContext);
    }

    /**
     * Constant to signal that nothing should be done
     * with the artifact after method return.
     */
    int NOTHING    = 0;
    /**
     * Constant to signal that the database timestamp
     * should be updated after method return.
     */
    int TOUCH      = 1;
    /**
     * Constant to signal that the artifact should be stored
     * after method return.
     */
    int STORE      = 2;
    /**
     * Constant to signal that the artifact fork a backgroud thread
     * and should be hold in memory till it signals that it has
     * finished its operation.
     */
    int BACKGROUND = 3;
    // int DELETE     = 4;
    // int FOREVER    = 5;

    /**
     * This method may be called from feed(), describe(), advance()
     * and out to signal what should happend with artefact after
     * the current method call returns.
     * @param action Valid values are NOTHING, TOUCH, STORE, BACKGROUND.
     */
    void afterCall(int action);

    /**
     * When send to background with a afterCall(BACKGROUND) this
     * method is to be called from the background thread to signal
     * that the background operation has ended.
     * @param action Same semantics as in afterCall.
     */
    void afterBackground(int action);

    /**
     * Returns true, if the object forked a background thread and has not
     * finished it yet.
     */
    boolean isInBackground();

    /**
     * Adds a background message for the current Artifact or Collection.
     *
     * @param msg The message.
     */
    void addBackgroundMessage(Message msg);

    /**
     * Returns the background messages of the current Artifact or Collection.
     *
     * @return the list of background messages.
     */
    LinkedList<Message> getBackgroundMessages();

    /**
     * Access to the global context of the runtime system.
     * @return The global context.
     */
    Object globalContext();

    /**
     * Access to the artifact database itself.
     * @return The database.
     */
    ArtifactDatabase getDatabase();

    /**
     * The meta data of the current call. Used to transport
     * language preferences of the callee e.g.
     * @return The meta information of this call.
     */
    CallMeta getMeta();

    /**
     * Each call context has a clipboard.
     * getContextValue is used to fetch data from this board.
     * @param key Key of the requested item.
     * @return The value stored for the specified value, null if
     *         no item with this key exists.
     */
    Object getContextValue(Object key);

    /**
     * Each call context has a clipboard.
     * putContextValue is used to store a key/value pair onto this board.
     * @param key   The key of the pair
     * @param value The value of the pair.
     * @return The formerly stored value under the given key.
     */
    Object putContextValue(Object key, Object value);

    /**
     * Returns the time to live of the current artifact.
     * @return The time to live of the current artifact.
     */
    Long getTimeToLive();

    /**
     * Get a list of DataProvider that get provide 'key' type of data to
     * other facets.
     */
    public List<DataProvider> getDataProvider(Object key);

    /**
     * Register a DataProvider that can provide 'key' type of data to
     * other facets.
     */
    public Object registerDataProvider(Object key, DataProvider provider);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
