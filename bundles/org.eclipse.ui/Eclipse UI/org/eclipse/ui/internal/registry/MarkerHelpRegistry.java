package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import java.util.TreeSet;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This class is a registry for marker help
 * contexts and resolutions.
 */


public class MarkerHelpRegistry implements IMarkerHelpRegistry {
	/**
	 * Table of queries for marker F1 help.
	 */
	private Map helpQueries = new HashMap();
	/**
	 * Table of queries for marker resolutions
	 */
	private Map resolutionQueries = new HashMap();
	/**
	 * Help context id attribute in configuration element
	 */
	private static final String ATT_HELP = "helpContextId"; //$NON-NLS-1$
	/**
	 * Resolution class attribute name in configuration element
	 */
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * Method declared on IMarkerHelpRegistry.
	 */
	public String getHelp(IMarker marker) {
		// Return the first match (we assume there is only one)
		for (Iterator iter = helpQueries.keySet().iterator(); iter.hasNext();) {
			MarkerQuery query = (MarkerQuery)iter.next();
			MarkerQueryResult result = query.performQuery(marker);
			if (result != null) {
				// See if a matching result is registered
				Map resultsTable = (Map)helpQueries.get(query);
				IConfigurationElement element = (IConfigurationElement)resultsTable.get(result);
				if (element != null)
					// We have a match so return the help context id
					return element.getAttribute(ATT_HELP);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * Method declared on IMarkerHelpRegistry.
	 */
	public boolean hasResolutions(IMarker marker) {
		// Detect a match
		for (Iterator iter = resolutionQueries.keySet().iterator(); iter.hasNext();) {
			MarkerQuery query = (MarkerQuery)iter.next();
			MarkerQueryResult result = query.performQuery(marker);
			if (result != null) {
				// See if a matching result is registered
				Map resultsTable = (Map)resolutionQueries.get(query);
				IConfigurationElement element = (IConfigurationElement)resultsTable.get(result);
				if (element != null) {
					IMarkerResolution resolution = null;
					if (element.getDeclaringExtension().getDeclaringPluginDescriptor().isPluginActivated()) {
						// The element's plugin is loaded so we instantiate the resolution
						try {
							resolution = (IMarkerResolution)element.createExecutableExtension(ATT_CLASS);						
						} catch (CoreException e) {
							WorkbenchPlugin.log("Unable to instantiate resolution", e.getStatus()); //$NON-NLS-1$
						}
						if (resolution != null) {
							resolution.init(marker);
							if (resolution.isAppropriate())
								return true;
						}
					} else {
						// The element's plugin in not loaded so we assume 
						// the marker is appropriate
						return true;
					}	
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * Method declared on IMarkerHelpRegistry.
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		// Collect all matches
		ArrayList resolutions = new ArrayList();
		for (Iterator iter = resolutionQueries.keySet().iterator(); iter.hasNext();) {
			MarkerQuery query = (MarkerQuery)iter.next();
			MarkerQueryResult result = query.performQuery(marker);
			if (result != null) {
				// See if a matching result is registered
				Map resultsTable = (Map)resolutionQueries.get(query);
				IConfigurationElement element = (IConfigurationElement)resultsTable.get(result);
				if (element != null) {
					IMarkerResolution resolution = null;
					if (element.getDeclaringExtension().getDeclaringPluginDescriptor().isPluginActivated()) {
						// The element's plugin is loaded so we instantiate the resolution
						try {
							resolution = (IMarkerResolution)element.createExecutableExtension(ATT_CLASS);						
						} catch (CoreException e) {
							WorkbenchPlugin.log("Unable to instantiate resolution", e.getStatus()); //$NON-NLS-1$
						}
						if (resolution != null) {
							resolution.init(marker);
							if (resolution.isAppropriate())
								resolutions.add(resolution);
						}
					} else {
						// The element's plugin in not loaded so we create a delegate resolution
						resolution = new MarkerResolutionDelegate(element);
						resolution.init(marker);
						resolutions.add(resolution);
					}	
				}
			}
		}
		return (IMarkerResolution[])resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	/**
	 * Adds a help query to the registry.
	 * 
	 * @param query a marker query
	 * @param result a result for the given query
	 * @param element the configuration element defining the result
	 */
	public void addHelpQuery(MarkerQuery query, MarkerQueryResult result, 
		IConfigurationElement element) {
		
		addQuery(helpQueries, query, result, element);
	}

	/**
	 * Adds a resolution query to the registry.
	 * 
	 * @param query a marker query
	 * @param result a result for the given query
	 * @param element the configuration element defining the result
	 */
	public void addResolutionQuery(MarkerQuery query, MarkerQueryResult result, 
		IConfigurationElement element) {
		
		addQuery(resolutionQueries, query, result, element);
	}

	/**
	 * Adds a query to the given table.
	 * 
	 * @param table the table to which the query is added
	 * @param query a marker query
	 * @param result a result for the given query
	 * @param element the configuration element defining the result
	 */
	private void addQuery(Map table, MarkerQuery query, MarkerQueryResult result, 
		IConfigurationElement element) {
		
		// See if the query is already in the table
		Map results = (Map)table.get(query);
		if (results == null) {
			// Create a new results table
			results = new HashMap();
			
			// Add the query to the table
			table.put(query, results);
		}
		
		// Add the new result
		results.put(result, element);
	}
}

