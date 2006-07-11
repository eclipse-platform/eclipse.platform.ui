/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.toc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.IToc;
import org.eclipse.help.ITocContribution;
import org.eclipse.help.ITocProvider;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.util.ProductPreferences;

import com.ibm.icu.util.StringTokenizer;

/*
 * Manages toc contributions (ITocContribution) supplied by the various toc
 * providers (ITocProvider).
 */
public class TocManager {
	
	private static final String EXTENSION_POINT_ID_TOC = HelpPlugin.PLUGIN_ID + ".toc"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_TOC_PROVIDER = "tocProvider"; //$NON-NLS-1$
	private static final String ATTRIBUTE_NAME_CLASS = "class"; //$NON-NLS-1$
	
	private ITocProvider[] tocProviders;
	private Map tocsByLocale = new HashMap();
	private Map tocsById = new HashMap();

	/*
	 * Returns all top-level toc entries (books) for the given locale.
	 */
	public synchronized IToc[] getTocs(String locale) {
		fetchTocs(locale);
		return (IToc[])tocsByLocale.get(locale);
	}

	/*
	 * Returns the toc whose toc contribution has the given id, for the
	 * given locale.
	 */
	public synchronized IToc getToc(String id, String locale) {
		fetchTocs(locale);
		return (IToc)tocsById.get(id);
	}
	
	private void fetchTocs(String locale) {
		IToc[] tocs = (IToc[])tocsByLocale.get(locale);
		if (tocs == null) {
			ITocContribution[] raw = getAllTocContributions(locale);
			ITocContribution[] filtered = filterTocContributions(raw);
			ITocContribution[] ordered = orderTocContributions(filtered);
			List orderedTocs = new ArrayList(ordered.length);
			for (int i=0;i<ordered.length;++i) {
				try {
					IToc toc = ordered[i].getToc();
					orderedTocs.add(toc);
					tocsById.put(ordered[i].getId(), toc);
				}
				catch (Throwable t) {
					// log and skip
					String msg = "Error getting " + IToc.class.getName() + " from " + ITocContribution.class.getName() + ": " + ordered[i]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					HelpPlugin.logError(msg, t);
				}
			}
			tocs = (IToc[])orderedTocs.toArray(new IToc[orderedTocs.size()]);
			tocsByLocale.put(locale, tocs);
		}
	}
	
	/*
	 * Filters the given contributions according to product preferences. If
	 * either the contribution's id or its category's id is listed in the
	 * ignoredTocs, filter the contribution.
	 */
	private ITocContribution[] filterTocContributions(ITocContribution[] unfiltered) {
		Set tocsToFilter = getIgnoredTocContributions();
		List filtered = new ArrayList();
		for (int i=0;i<unfiltered.length;++i) {
			if (!tocsToFilter.contains(unfiltered[i].getId()) &&
					!tocsToFilter.contains(unfiltered[i].getCategoryId())) {
				filtered.add(unfiltered[i]);
			}
		}
		return (ITocContribution[])filtered.toArray(new ITocContribution[filtered.size()]);
	}
	
	/*
	 * Returns all toc contributions for the given locale, from all toc
	 * providers.
	 */
	private ITocContribution[] getAllTocContributions(String locale) {
		List contributions = new ArrayList();
		ITocProvider[] providers = getTocProviders();
		for (int i=0;i<providers.length;++i) {
			ITocContribution[] contrib;
			try {
				contrib = providers[i].getTocContributions(locale);
			}
			catch (Throwable t) {
				// log and skip
				String msg = "Error getting " + ITocContribution.class.getName() + " from " + ITocProvider.class.getName() + ": " + providers[i].getClass().getName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				HelpPlugin.logError(msg, t);
				continue;
			}
			
			// check for nulls
			for (int j=0;j<contrib.length;++j) {
				// null means no contribution
				if (contrib[j] != null) {
					// pre-fetch everything and cache for safety
					try {
						ITocContribution wrapped = new CachedTocContribution(contrib[j]);
						contributions.add(wrapped);
					}
					catch (Throwable t) {
						// log, and skip this offending contribution
						String msg = "Error getting " + ITocContribution.class.getName() + " information from " + contrib[j].getClass().getName(); //$NON-NLS-1$ //$NON-NLS-2$
						HelpPlugin.logError(msg, t);
						continue;
					}
				}
			}
		}
		return (ITocContribution[])contributions.toArray(new ITocContribution[contributions.size()]);
	}
	
	private Set getIgnoredTocContributions() {
		HashSet ignored = new HashSet();
		Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
		String preferredTocs = pref.getString(HelpPlugin.IGNORED_TOCS_KEY);
		if (preferredTocs.length() > 0) {
			StringTokenizer suggestdOrderedInfosets = new StringTokenizer(preferredTocs, " ;,"); //$NON-NLS-1$
			while (suggestdOrderedInfosets.hasMoreTokens()) {
				ignored.add(suggestdOrderedInfosets.nextToken());
			}
		}
		return ignored;
	}

	/*
	 * Returns all registered toc providers (potentially cached).
	 */
	private ITocProvider[] getTocProviders() {
		if (tocProviders == null) {
			List providers = new ArrayList();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(EXTENSION_POINT_ID_TOC);
			for (int i=0;i<elements.length;++i) {
				IConfigurationElement elem = elements[i];
				try {
					if (elem.getName().equals(ELEMENT_NAME_TOC_PROVIDER)) {
						String className = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
						if (className != null) {
							try {
								ITocProvider provider = (ITocProvider)elem.createExecutableExtension(ATTRIBUTE_NAME_CLASS);
								providers.add(provider);
							}
							catch (CoreException e) {
								// log and skip
								String msg = "Error instantiating " + ELEMENT_NAME_TOC_PROVIDER + " class"; //$NON-NLS-1$ //$NON-NLS-2$
								HelpPlugin.logError(msg, e);
							}
							catch (ClassCastException e) {
								// log and skip
								String msg = ELEMENT_NAME_TOC_PROVIDER + " class must implement " + ITocProvider.class.getName(); //$NON-NLS-1$
								HelpPlugin.logError(msg, e);
							}
						}
						else {
							// log the missing class attribute and skip
							String msg = ELEMENT_NAME_TOC_PROVIDER + " element of extension point " + EXTENSION_POINT_ID_TOC + " must specify a " + ATTRIBUTE_NAME_CLASS + " attribute"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			tocProviders = (ITocProvider[])providers.toArray(new ITocProvider[providers.size()]);
		}
		return tocProviders;
	}

	/*
	 * Orders the given toc contributions by category and product preference.
	 */
	private ITocContribution[] orderTocContributions(ITocContribution[] unorderedTocs) {
		// first categorize the TOCs
		List itemsToOrder = new ArrayList();
		Map categorized = categorizeTocs(Arrays.asList(unorderedTocs), itemsToOrder);
			
		// order them
		List orderedItems = ProductPreferences.getOrderedList(HelpPlugin.getDefault(), HelpPlugin.BASE_TOCS_KEY, itemsToOrder);
			
		// replace with actual ITocContribution or category
		orderedItems = substituteValues(orderedItems, categorized);
			
		// expand the categories
		orderedItems = expandCategories(orderedItems);
		return (ITocContribution[])orderedItems.toArray(new ITocContribution[orderedItems.size()]);
	}
	
	/*
	 * Categorizes the given toc contributions into categories, or individual
	 * toc contributions if no category (treated as a category of one). Returns
	 * mapping from category id/toc id to category/toc. Order of categories/
	 * tocs is returned via tocOrder.
	 */
	private Map categorizeTocs(List tocs, List tocOrder) {
		Map categorized = new HashMap();
		Iterator iter = tocs.iterator();
		while (iter.hasNext()) {
			ITocContribution toc = (ITocContribution)iter.next();
			String categoryId;
			try {
				categoryId = toc.getCategoryId();
			}
			catch (Throwable t) {
				// log and skip
				String msg = "Error retrieving categoryId from " + ITocContribution.class.getName() + ": " + toc.getClass().getName(); //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg, t);
				continue;
			}
			if (categoryId != null) {
				// it has a category, add it to the appropriate TocCategory
				TocCategory category = (TocCategory)categorized.get(categoryId);
				if (category == null) {
					// create categories as needed
					category = new TocCategory(categoryId);
					categorized.put(categoryId, category);
					tocOrder.add(categoryId);
				}
				category.add(toc);
			}
			else {
				// doesn't have a category; insert the TOC directly
				String id;
				try {
					id = toc.getId();
				}
				catch (Throwable t) {
					// log and skip
					String msg = "Error retrieving id from " + ITocContribution.class.getName() + ": " + toc.getClass().getName(); //$NON-NLS-1$ //$NON-NLS-2$
					HelpPlugin.logError(msg, t);
					continue;
				}
				categorized.put(id, toc);
				tocOrder.add(id);
			}
		}
		return categorized;
	}
	
	/*
	 * Expands all categories in the given list to actual toc contributions
	 * organized by category.
	 */
	private List expandCategories(List entries) {
		List expanded = new ArrayList();
		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			Object entry = iter.next();
			if (entry instanceof ITocContribution) {
				expanded.add(entry);
			}
			else if (entry instanceof TocCategory) {
				expanded.addAll((TocCategory)entry);
			}
		}
		return expanded;
	}

	/*
	 * Substitutes each item with it's corresponding mapping from the map.
	 * Original List is not modified.
	 */
	private static List substituteValues(List items, Map map) {
		if (items != null && map != null) {
			List result = new ArrayList(items.size());
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				Object value = map.get(key);
				if (value != null) {
					result.add(value);
				}
			}
			return result;
		}
		return null;
	}
	
	/*
	 * A category of tocs. A category has an id and a list of contained
	 * tocs.
	 */
	private static class TocCategory extends ArrayList {
		
		private static final long serialVersionUID = 1L;
		private String id;
		
		/**
		 * Constructs a new empty TOC category with the given id.
		 * 
		 * @param id the category's id
		 */
		public TocCategory(String id) {
			this.id = id;
		}
		
		/**
		 * Returns the category's id.
		 * 
		 * @return the id of the category
		 */
		public String getId() {
			return id;
		}
	}
}
