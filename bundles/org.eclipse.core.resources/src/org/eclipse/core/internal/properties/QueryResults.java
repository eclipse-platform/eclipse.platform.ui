package org.eclipse.core.internal.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * This type acts as a container for the results of
 * a query over the property store.
 * <p>
 * Where the results of a query are potentially spread across
 * multiple resources, the results are gathered together in a
 * single query results object.</p>
 *
 */
import java.util.*;
//
public class QueryResults {
	protected HashMap table = new HashMap(10);
public QueryResults() {
	super();
}
protected void add(ResourceName resourceName, Object value) {
	List properties = getResults(resourceName);
	if (properties.isEmpty())
		table.put(resourceName, properties);
	if (properties.indexOf(value) == -1)
		properties.add(value);
}
/**
 * Answers with an <code>Enumeration</code> of resources that comprise
 * the result.
 *
 * @return an <code>Enumeration</code> of <code>ResourceName</code>,
 *  or an empty enumerator if there were no matching resources.
 */
public Enumeration getResourceNames() {
	return Collections.enumeration(table.keySet());
}
/**
 * Returns all the results for a given resource.
 *
 * @param resourceName the resource for which the results are sought.
 * @return a <code>List</code> of the matching results. The <code>List</code>
 *  will be empty if there are no matching results.
 */
public List getResults(ResourceName resourceName) {
	List results = (List) table.get(resourceName);
	if (results == null)
		results = new ArrayList(10);
	return results;
}
}
