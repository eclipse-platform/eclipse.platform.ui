/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMarkerHelpContextProvider;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;

/**
 * This class is a registry for marker help contexts and resolutions.
 */
public class MarkerHelpRegistry implements IMarkerHelpRegistry {
	/**
	 * Table of queries for marker F1 help.
	 */
	private Map<MarkerQuery, Map<MarkerQueryResult, Collection<IConfigurationElement>>> helpQueries;

	/**
	 * Sorted list of help queries. Used to ensure that the "most specific"
	 * query is tried first
	 */
	private List<MarkerQuery> sortedHelpQueries;

	/**
	 * Table of queries for marker resolutions
	 */
	private Map<MarkerQuery, Map<MarkerQueryResult, Collection<IConfigurationElement>>> resolutionQueries;

	/**
	 * Help context id attribute in configuration element
	 */
	private static final String ATT_HELP = "helpContextId"; //$NON-NLS-1$

	/**
	 * Help context provider attribute name in configuration element
	 */
	private static final String ATT_PROVIDER = "helpContextProvider"; //$NON-NLS-1$

	/**
	 * Placeholder for errors in generator
	 */
	private static final IMarkerResolutionGenerator GENERATOR_ERROR = marker -> null;

	/**
	 * Placeholder for not yet active generators
	 */
	private static final IMarkerResolutionGenerator GENERATOR_NOT_ACTIVE = marker -> new IMarkerResolution[0];

	/**
	 * Placeholder for not yet started generators
	 */
	private static final IMarkerResolutionGenerator GENERATOR_NOT_STARTED = marker -> new IMarkerResolution[0];

	/**
	 * Placeholder for errors in help provider
	 */
	private static final IMarkerHelpContextProvider DUMMY_HELP_PROVIDER = new IMarkerHelpContextProvider() {

		@Override
		public boolean hasHelpContextForMarker(IMarker marker) {
			return false;
		}

		@Override
		public String getHelpContextForMarker(IMarker marker) {
			return null;
		}
	};

	/**
	 * Map of known marker resolution generators
	 */
	private Map<IConfigurationElement, IMarkerResolutionGenerator> generatorMap;

	/**
	 * Map of known marker help context providers
	 */
	private Map<IConfigurationElement, IMarkerHelpContextProvider> helpProviderMap;

	/**
	 * Resolution class attribute name in configuration element
	 */
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private static class QueryComparator implements Comparator<MarkerQuery> {
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof QueryComparator)) {
				return false;
			}
			return true;
		}

		@Override
		public int compare(MarkerQuery q1, MarkerQuery q2) {
			// more attribues come first
			int size1 = q1.getAttributes().length;
			int size2 = q2.getAttributes().length;

			if (size1 > size2) {
				return -1;
			}
			if (size1 == size2) {
				return 0;
			}
			return 1;
		}
	}

	public MarkerHelpRegistry() {
		helpQueries = new HashMap<>();
		resolutionQueries = new LinkedHashMap<>();
		generatorMap = new HashMap<>();
		helpProviderMap = new HashMap<>();
	}

	@Override
	public String getHelp(IMarker marker) {
		if (sortedHelpQueries == null) {
			Set<MarkerQuery> set = helpQueries.keySet();
			sortedHelpQueries = new ArrayList<>(set.size());
			sortedHelpQueries.addAll(set);
			sortedHelpQueries.sort(new QueryComparator());
		}

	    // Return the first match (we assume there is only one)
		for (MarkerQuery query : sortedHelpQueries) {
			MarkerQueryResult result = query.performQuery(marker);
			if (result != null) {
				// See if a matching result is registered
				Map<MarkerQueryResult, Collection<IConfigurationElement>> resultsTable = helpQueries.get(query);

				if (resultsTable.containsKey(result)) {

					Iterator<IConfigurationElement> elements = resultsTable.get(result).iterator();
					while (elements.hasNext()) {
						IConfigurationElement element = elements.next();
						// We have a match so check whether the element has a helpContextProvider
						String helpContextProvider = element.getAttribute(ATT_PROVIDER);
						if (helpContextProvider == null) {
							// It does not have a helpContextProvider. Return the static helpContextId.
							return element.getAttribute(ATT_HELP);
						}
						// It has a helpContextProvider. Use it to get a help context id
						IMarkerHelpContextProvider provider = createHelpProvider(element);
						String res;
						if (provider.hasHelpContextForMarker(marker)
								&& (res = provider.getHelpContextForMarker(marker)) != null) {
							return res;
						}
					}
				}
			}
		}
		return null;
	}

	private IMarkerHelpContextProvider createHelpProvider(IConfigurationElement element) {
		IMarkerHelpContextProvider provider = getHelpProvider(element);
		if (provider == null) {
			try {
				provider = (IMarkerHelpContextProvider) element.createExecutableExtension(ATT_PROVIDER);
			} catch (CoreException e) {
				provider = DUMMY_HELP_PROVIDER;
				Policy.handle(e);
			}
		}
		putHelpProvider(element, provider);
		return provider;
	}

	@Override
	public boolean hasResolutions(IMarker marker) {
		// Detect a match
		for (Entry<MarkerQuery, Map<MarkerQueryResult, Collection<IConfigurationElement>>> entry : resolutionQueries
				.entrySet()) {
			MarkerQuery query = entry.getKey();
			MarkerQueryResult result = query.performQuery(marker);
			if (result != null) {
				// See if a matching result is registered
				Map<MarkerQueryResult, Collection<IConfigurationElement>> resultsTable = entry.getValue();

				if (resultsTable.containsKey(result)) {

					Iterator<IConfigurationElement> elements = resultsTable.get(result).iterator();
					while (elements.hasNext()) {
						IConfigurationElement element = elements.next();
						IMarkerResolutionGenerator generator = createGenerator(element);
						if (hasResolution(marker, element, generator)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Return whether or not this configuration element has a resolution for the
	 * marker.
	 *
	 * @return boolean <code>true</code> if there is a resolution.
	 */
	private boolean hasResolution(IMarker marker, IConfigurationElement element, IMarkerResolutionGenerator generator) {
		if (generator == null || generator == GENERATOR_ERROR) {
			// error happened, no resolution here
			return false;
		}
		if (generator == GENERATOR_NOT_ACTIVE || generator == GENERATOR_NOT_STARTED) {
			// The element's plugin in not loaded so we assume
			// the generator will produce resolutions for the marker
			return true;
		}
		if (generator instanceof IMarkerResolutionGenerator2) {
			if (((IMarkerResolutionGenerator2) generator).hasResolutions(marker)) {
				return true;
			}
		} else {
			IMarkerResolution[] resolutions = generator.getResolutions(marker);
			if (resolutions == null) {
				StatusManager.getManager().handle(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
						IStatus.ERROR, "Failure in " + generator.getClass().getName() + //$NON-NLS-1$
								" from plugin " + element.getContributor().getName() + //$NON-NLS-1$
								": getResolutions(IMarker) must not return null", //$NON-NLS-1$
						null), StatusManager.LOG);

				return false;
			} else if (resolutions.length > 0) {
				// there is at least one resolution
				return true;
			}
		}
		return false;
	}

	private IMarkerResolutionGenerator createGenerator(IConfigurationElement element) {
		IMarkerResolutionGenerator generator = getGenerator(element);
		if (generator != null && generator != GENERATOR_NOT_STARTED) {
			return generator;
		}
		Bundle bundle = Platform.getBundle(element.getContributor().getName());
		if (canLoadExtensionWithoutActivation(bundle)) {
			// The element's plugin is loaded so we instantiate
			// the resolution
			generator = createGeneratorFromActiveBundle(element);
		} else {
			BundleContext bundleContext = bundle.getBundleContext();
			if (bundleContext != null) {
				generator = GENERATOR_NOT_ACTIVE;
				bundleContext.addBundleListener(new BundleListener() {
					@Override
					public void bundleChanged(BundleEvent b) {
						if (b.getType() == BundleEvent.STARTED && canLoadExtensionWithoutActivation(bundle)) {
							bundleContext.removeBundleListener(this);
							if (getGenerator(element) == GENERATOR_NOT_ACTIVE) {
								putGenerator(element, null);
							}
						}
					}
				});
				// In case bundle state changed after the first call
				if (canLoadExtensionWithoutActivation(bundle)) {
					generator = createGeneratorFromActiveBundle(element);
				}
			} else {
				generator = GENERATOR_NOT_STARTED;
			}
		}
		putGenerator(element, generator);
		return generator;
	}

	private static boolean canLoadExtensionWithoutActivation(Bundle bundle) {
		int state = bundle.getState();
		if (state == Bundle.ACTIVE) {
			return true;
		}
		if (state == Bundle.RESOLVED) {
			Dictionary<String, String> manifest = bundle.getHeaders();
			if (manifest.get(Constants.BUNDLE_ACTIVATOR) == null
					&& manifest.get(Constants.BUNDLE_ACTIVATIONPOLICY) == null) {
				// Allow loading classes from bundles that will not automatically activate
				return true;
			}
		}
		return false;
	}

	private static IMarkerResolutionGenerator createGeneratorFromActiveBundle(IConfigurationElement element) {
		IMarkerResolutionGenerator generator;
		try {
			generator = (IMarkerResolutionGenerator) element.createExecutableExtension(ATT_CLASS);
		} catch (CoreException e) {
			Policy.handle(e);
			generator = GENERATOR_ERROR;
		}
		return generator;
	}

	private IMarkerResolutionGenerator getGenerator(IConfigurationElement element) {
		synchronized (generatorMap) {
			return generatorMap.get(element);
		}
	}

	private IMarkerResolutionGenerator putGenerator(IConfigurationElement element,
			IMarkerResolutionGenerator generator) {
		synchronized (generatorMap) {
			return generatorMap.put(element, generator);
		}
	}

	private IMarkerHelpContextProvider getHelpProvider(IConfigurationElement element) {
		synchronized (helpProviderMap) {
			return helpProviderMap.get(element);
		}
	}

	private IMarkerHelpContextProvider putHelpProvider(IConfigurationElement element,
			IMarkerHelpContextProvider generator) {
		synchronized (helpProviderMap) {
			return helpProviderMap.put(element, generator);
		}
	}

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		// Collect all matches
		ArrayList<IMarkerResolution> resolutions = new ArrayList<>();
		for (Entry<MarkerQuery, Map<MarkerQueryResult, Collection<IConfigurationElement>>> resolutionQueryEntry : resolutionQueries
				.entrySet()) {
			Entry<MarkerQuery, Map<MarkerQueryResult, Collection<IConfigurationElement>>> entry = resolutionQueryEntry;
			MarkerQuery query = entry.getKey();
			MarkerQueryResult result = query.performQuery(marker);
			if (result != null) {
				// See if a matching result is registered
				Map<MarkerQueryResult, Collection<IConfigurationElement>> resultsTable = entry.getValue();
				if (resultsTable.containsKey(result)) {
					Iterator<IConfigurationElement> elements = resultsTable.get(result).iterator();
					while (elements.hasNext()) {
						IConfigurationElement element = elements.next();
						IMarkerResolutionGenerator generator = null;
						try {
							generator = (IMarkerResolutionGenerator) element.createExecutableExtension(ATT_CLASS);
							IMarkerResolution[] res = generator.getResolutions(marker);
							if (res != null) {
								resolutions.addAll(Arrays.asList(res));
							} else {
								StatusManager.getManager()
								.handle(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
										IStatus.ERROR, "Failure in " + generator.getClass().getName() + //$NON-NLS-1$
										" from plugin " + element.getContributor().getName() + //$NON-NLS-1$
										": getResolutions(IMarker) must not return null", //$NON-NLS-1$
										null), StatusManager.LOG);
							}
						} catch (CoreException e) {
							Policy.handle(e);
						}
					}
				}
			}
		}
		return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
	}

	/**
	 * Adds a help query to the registry.
	 *
	 * @param query
	 *            a marker query
	 * @param result
	 *            a result for the given query
	 * @param element
	 *            the configuration element defining the result
	 */
	public void addHelpQuery(MarkerQuery query, MarkerQueryResult result,
			IConfigurationElement element) {

		addQuery(helpQueries, query, result, element);
	}

	/**
	 * Adds a resolution query to the registry.
	 *
	 * @param query
	 *            a marker query
	 * @param result
	 *            a result for the given query
	 * @param element
	 *            the configuration element defining the result
	 */
	public void addResolutionQuery(MarkerQuery query, MarkerQueryResult result,
			IConfigurationElement element) {

		addQuery(resolutionQueries, query, result, element);
	}

	/**
	 * Adds a query to the given table.
	 *
	 * @param table
	 *            the table to which the query is added
	 * @param query
	 *            a marker query
	 * @param result
	 *            a result for the given query
	 * @param element
	 *            the configuration element defining the result
	 */
	private void addQuery(Map<MarkerQuery, Map<MarkerQueryResult, Collection<IConfigurationElement>>> table,
			MarkerQuery query,
			MarkerQueryResult result, IConfigurationElement element) {

		// See if the query is already in the table
		Map<MarkerQueryResult, Collection<IConfigurationElement>> results = table.get(query);
		if (results == null) {
			// Create a new results table
			results = new HashMap<>();

			// Add the query to the table
			table.put(query, results);
		}

		if (results.containsKey(result)) {
			Collection<IConfigurationElement> currentElements = results.get(result);
			currentElements.add(element);
		} else {
			Collection<IConfigurationElement> elements = new HashSet<>();
			elements.add(element);

			// Add the new result
			results.put(result, elements);
		}
	}
}
