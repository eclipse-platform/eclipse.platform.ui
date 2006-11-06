/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation - 122967 [Help] Remote help system
 *                       163558 Dynamic content support for all UA
 *******************************************************************************/
package org.eclipse.help.internal.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.AbstractIndexProvider;
import org.eclipse.help.IIndex;
import org.eclipse.help.IndexContribution;
import org.eclipse.help.internal.HelpPlugin;

public class IndexManager {

	private static final String EXTENSION_POINT_ID_INDEX = HelpPlugin.PLUGIN_ID + ".index"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_INDEX_PROVIDER = "indexProvider"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_CLASS = "class"; //$NON-NLS-1$
	
	private Map indexContributionsByLocale = new HashMap();
	private Map indexesByLocale = new HashMap();
	private AbstractIndexProvider[] indexProviders;
	
	public synchronized IIndex getIndex(String locale) {
		Index index = (Index)indexesByLocale.get(locale);
		if (index == null) {
			List contributions = Arrays.asList(getIndexContributions(locale));
			filterIndexContributions(contributions);
			IndexAssembler assembler = new IndexAssembler();
			index = assembler.assemble(contributions, locale);
			indexesByLocale.put(locale, index);
		}
		return index;
	}
	
	/*
	 * Returns all index contributions for the given locale, from all
	 * providers.
	 */
	public synchronized IndexContribution[] getIndexContributions(String locale) {
		IndexContribution[] cached = (IndexContribution[])indexContributionsByLocale.get(locale);
		if (cached == null) {
			List contributions = new ArrayList();
			AbstractIndexProvider[] providers = getIndexProviders();
			for (int i=0;i<providers.length;++i) {
				IndexContribution[] contrib;
				try {
					contrib = providers[i].getIndexContributions(locale);
				}
				catch (Throwable t) {
					// log, and skip the offending provider
					String msg = "Error getting " + IndexContribution.class.getName() + " from " + AbstractIndexProvider.class.getName() + ": " + providers[i].getClass().getName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					HelpPlugin.logError(msg, t);
					continue;
				}
				
				// check for nulls
				for (int j=0;j<contrib.length;++j) {
					// null means no contribution
					if (contrib[j] != null) {
						contributions.add(contrib[j]);
					}
				}
			}
			cached = (IndexContribution[])contributions.toArray(new IndexContribution[contributions.size()]);
			indexContributionsByLocale.put(locale, cached);
		}
		return cached;
	}
	
	/*
	 * Clears all cached contributions, forcing the manager to query the
	 * providers again next time a request is made.
	 */
	public void clearCache() {
		indexContributionsByLocale.clear();
		indexesByLocale.clear();
	}

	/*
	 * Returns all registered index providers (potentially cached).
	 */
	private AbstractIndexProvider[] getIndexProviders() {
		if (indexProviders == null) {
			List providers = new ArrayList();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_POINT_ID_INDEX);
			for (int i=0;i<elements.length;++i) {
				IConfigurationElement elem = elements[i];
				try {
					if (elem.getName().equals(ELEMENT_NAME_INDEX_PROVIDER)) {
						String className = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
						if (className != null) {
							try {
								AbstractIndexProvider provider = (AbstractIndexProvider)elem.createExecutableExtension(ATTRIBUTE_NAME_CLASS);
								providers.add(provider);
							}
							catch (CoreException e) {
								// log and skip
								String msg = "Error instantiating " + ELEMENT_NAME_INDEX_PROVIDER + " class"; //$NON-NLS-1$ //$NON-NLS-2$
								HelpPlugin.logError(msg, e);
							}
							catch (ClassCastException e) {
								// log and skip
								String msg = ELEMENT_NAME_INDEX_PROVIDER + " class must implement " + AbstractIndexProvider.class.getName(); //$NON-NLS-1$
								HelpPlugin.logError(msg, e);
							}
						}
						else {
							// log the missing class attribute and skip
							String msg = ELEMENT_NAME_INDEX_PROVIDER + " element of extension point " + EXTENSION_POINT_ID_INDEX + " must specify a " + ATTRIBUTE_NAME_CLASS + " attribute"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							try {
								msg += " (declared from plug-in " + elem.getNamespaceIdentifier() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
							}
							catch (InvalidRegistryObjectException e) {
								// skip the declaring plugin part
							}
							HelpPlugin.logError(msg, null);
						}
					}
				}
				catch (InvalidRegistryObjectException e) {
					// no longer valid; skip it
				}
			}
			indexProviders = (AbstractIndexProvider[])providers.toArray(new AbstractIndexProvider[providers.size()]);
		}
		return indexProviders;
	}
	
	/*
	 * Returns whether or not the index has been completely loaded for the
	 * given locale yet or not.
	 */
	public boolean isIndexLoaded(String locale) {
		return indexesByLocale.get(locale) != null;
	}
	
	/*
	 * Filters the given contributions according to product preferences. If
	 * either the contribution's id or its category's id is listed in the
	 * ignoredIndexes, filter the contribution.
	 */
	private void filterIndexContributions(List unfiltered) {
		Set indexesToFilter = getIgnoredIndexContributions();
		ListIterator iter = unfiltered.listIterator();
		while (iter.hasNext()) {
			IndexContribution contribution = (IndexContribution)iter.next();
			if (indexesToFilter.contains(contribution.getId())) {
				iter.remove();
			}
		}
	}

	private Set getIgnoredIndexContributions() {
		HashSet ignored = new HashSet();
		Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
		String preferredIndexes = pref.getString(HelpPlugin.IGNORED_INDEXES_KEY);
		if (preferredIndexes.length() > 0) {
			StringTokenizer suggestdOrderedInfosets = new StringTokenizer(preferredIndexes, " ;,"); //$NON-NLS-1$
			while (suggestdOrderedInfosets.hasMoreTokens()) {
				ignored.add(suggestdOrderedInfosets.nextToken());
			}
		}
		return ignored;
	}
}
