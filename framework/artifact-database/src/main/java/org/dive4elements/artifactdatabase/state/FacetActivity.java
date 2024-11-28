package org.dive4elements.artifactdatabase.state;

import org.dive4elements.artifacts.Artifact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * System used in practice used by AttributeWriter in flys-artifacts to decide
 * whether a facet is initially active.
 * Provides a singleton Registry into which FacetActivities can be registered
 * under a key (in practice the artifacts name.  This Registry is queried for
 * new Facets in order to find whether they are active or inactive.
 */
public interface FacetActivity
{
    /** Static 'activity' that lets all facets be active. */
    public static final FacetActivity ACTIVE = new FacetActivity() {
        @Override
        public Boolean isInitialActive(
            Artifact artifact,
            Facet    facet,
            String   output
        ) {
            return Boolean.TRUE;
        }
    };

    /** Static 'activity' that lets all facets be inactive. */
    public static final FacetActivity INACTIVE = new FacetActivity() {
        @Override
        public Boolean isInitialActive(
            Artifact artifact,
            Facet    facet,
            String   output
        ) {
            return Boolean.FALSE;
        }
    };

    Boolean isInitialActive(Artifact artifact, Facet facet, String output);

    /** Singleton registry, that maps artifact names to the activities, which
     * decide whether or not a facet should be (initially) active. */
    public static final class Registry {

        /** The logger for this class. */
        private static Logger logger = LogManager.getLogger(Registry.class);

        /** Singleton instance. */
        private static final Registry INSTANCE = new Registry();

        /** Map of keys (artifact names) to the activities. */
        private Map<String, List<FacetActivity>> activities;

        /** Private singleton constructor for the Facet-Activity-Registry. */
        private Registry() {
            activities = new HashMap<String, List<FacetActivity>>();
        }

        /** Access Singleton instance. */
        public static Registry getInstance() {
            return INSTANCE;
        }

        /** Queries whether a given facet should be active or not. */
        public synchronized boolean isInitialActive(
            String   key,
            Artifact artifact,
            Facet    facet,
            String   output
        ) {
            List<FacetActivity> activityList = activities.get(key);
            if (activityList == null) {
                logger.debug("FacetActivity.Registry: No activity " +
                             "registered for " + key);
                return true;
            }
            if (activityList.size() != 1) {
                logger.warn("FacetActivity.Registry: More than one " +
                            "FacetActivity registered for " + key);
            }
            for (FacetActivity activity: activityList) {
                Boolean isActive =
                    activity.isInitialActive(artifact, facet, output);
                // Nice. Only, in practice they never return NULL.
                if (isActive != null) {
                    return isActive;
                }
            }
            return true;
        }


        /** Add a FacetActivity under given key (usually artifacts name). */
        public synchronized void register(String key, FacetActivity activity) {
            List<FacetActivity> activityList = activities.get(key);
            if (activityList == null) {
                activityList = new ArrayList<FacetActivity>(3);
                activities.put(key, activityList);
            }
            activityList.add(activity);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
