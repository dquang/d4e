package org.dive4elements.artifacts;

/**
 * DataProviders register on a Blackboard with a key (basically shouting
 * "I can or know X!").
 *
 * Consumers look at the blackboard and then consume data from these
 * DataProvider, passing them (optional) parameterization and the blackboard
 * itself.
 *
 * Through the blackboard-passing-when-consuming, also recursive patterns can
 * be modelled (but take care, there is no in-built cycle detection).
 */
public interface DataProvider {
    /** Register this DataProvider on a blackboard under a key. */
    public void register(CallContext blackboard);

    /** Provide data, given parameterization and a "blackboard". */
    public Object provideData(Object key, Object param, CallContext context);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
