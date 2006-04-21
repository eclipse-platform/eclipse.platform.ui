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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.model.*;
import org.eclipse.help.internal.util.ProductPreferences;

/**
 * Manages the navigation model. It keeps track of all the tables of contents.
 */
public class TocManager {
	public static final String TOC_XP_NAME = "toc"; //$NON-NLS-1$
	public static final String TOC_ELEMENT_NAME = "toc"; //$NON-NLS-1$
	public static final String INDEX_ELEMENT_NAME = "index"; //$NON-NLS-1$

	/**
	 * Map of ITocNavNode[] by String
	 */
	private Map tocsByLang;

	/**
	 * Map of plugin ID (String) to index path (String)
	 */
	private Map contributingPlugins2IndexPaths;

	/**
	 * HelpNavigationManager constructor.
	 */
	public TocManager() {
		super();
		try {
			tocsByLang = new HashMap();
			// build TOCs for machine locale at startup
			// Note: this can be removed, and build on first invocation...
			build(Platform.getNL());
		} catch (Exception e) {
			HelpPlugin.logError("", e); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the list of TOC's available in the help system
	 */
	public ITocElement[] getTocs(String locale) {

		if (locale == null)
			return new ITocElement[0];

		ITocElement[] tocs = (ITocElement[]) tocsByLang.get(locale);
		if (tocs == null) {
			synchronized (this) {
				if (tocs == null) {
					build(locale);
				}
			}
			tocs = (ITocElement[]) tocsByLang.get(locale);
			// one more sanity test...
			if (tocs == null)
				tocs = new ITocElement[0];
		}
		return tocs;
	}

	/**
	 * Returns the navigation model for specified toc
	 */
	public ITocElement getToc(String href, String locale) {
		if (href == null || href.equals("")) //$NON-NLS-1$
			return null;
		ITocElement[] tocs = getTocs(locale);

		for (int i = 0; i < tocs.length; i++) {
			if (tocs[i].getHref().equals(href))
				return tocs[i];
		}
		return null;
	}

	/**
	 * Returns the list of contributing Bundle IDs
	 */
	public Collection getContributingPlugins() {
		if (contributingPlugins2IndexPaths == null) {
			getContributedTocFiles(Locale.getDefault().toString());
		}
		return contributingPlugins2IndexPaths.keySet();
	}

	/**
	 * Returns the index path for a given plugin ID
	 * @return String or null
	 */
	public String getIndexPath(String pluginId) {
		if (contributingPlugins2IndexPaths == null) {
			getContributedTocFiles(Locale.getDefault().toString());
		}
		return (String) contributingPlugins2IndexPaths.get(pluginId);
	}

	/**
	 * Builds the toc from the contribution files
	 */
	private void build(String locale) {
		IToc[] tocs;
		try {
			Collection contributedTocFiles = getContributedTocFiles(locale);
			TocBuilder builder = new TocBuilder();
			builder.build(contributedTocFiles);
			List builtTocs = builder.getBuiltTocs();
			tocs = new ITocElement[builtTocs.size()];
			int i = 0;
			for (Iterator it = builtTocs.iterator(); it.hasNext();) {
				tocs[i++] = (ITocElement) it.next();
			}
			List orderedTocs = orderTocs(builtTocs);
			tocs = new ITocElement[orderedTocs.size()];
			orderedTocs.toArray(tocs);
		} catch (Exception e) {
			tocs = new IToc[0];
			HelpPlugin.logError("", e); //$NON-NLS-1$
		}
		tocsByLang.put(locale, tocs);
	}

	/**
	 * Expands the given list of TOCs and categories into the full list of TOCs. This is
	 * done by substituting each category with all the TOCs in that category.
	 * 
	 * @param entries the TOCs and categories
	 * @return full list of TOCs
	 */
	private List expandCategories(List entries) {
		List expanded = new ArrayList();
		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			Object entry = iter.next();
			if (entry instanceof ITocElement) {
				expanded.add(entry);
			}
			else if (entry instanceof TocCategory) {
				expanded.addAll((TocCategory)entry);
			}
		}
		return expanded;
	}
	
	/**
	 * Orders the TOCs according to all products' preferences, with the
	 * active product getting precedence.
	 */
	private List orderTocs(List unorderedTocs) {
		try {
			// first categorize the TOCs
			Map categorized = categorizeTocs(unorderedTocs);
			
			// get the toc hrefs / category ids to be ordered
			List itemsToOrder = new ArrayList(categorized.keySet());
			
			// order them
			List orderedItems = ProductPreferences.getOrderedList(HelpPlugin.getDefault(), HelpPlugin.BASE_TOCS_KEY, itemsToOrder);
			
			// replace with actual Toc or TocCategory
			orderedItems = substituteValues(orderedItems, categorized);
			
			// expand the categories
			orderedItems = expandCategories(orderedItems);
			return orderedItems;
		}
		catch (Exception e) {
			// if anything goes wrong, log an error and fall back to unordered tocs
			HelpPlugin.logError("An unexpected internal error occured while ordering TOCs", e); //$NON-NLS-1$
			return unorderedTocs;
		}
	}

	/**
	 * Reads preferences to determine TOCs to be ignored.
	 * @return the list of TOC href's.
	 */
	private Collection getIgnoredTocs() {
		HashSet ignored = new HashSet();
		try {
			Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
			String preferredTocs = pref.getString(HelpPlugin.IGNORED_TOCS_KEY);
			if (preferredTocs != null) {
				StringTokenizer suggestdOrderedInfosets = new StringTokenizer(
						preferredTocs, " ;,"); //$NON-NLS-1$

				while (suggestdOrderedInfosets.hasMoreElements()) {
					ignored.add(suggestdOrderedInfosets.nextElement());
				}
			}
		} catch (Exception e) {
			HelpPlugin.logError(
					"Problems occurred reading plug-in preferences.", e); //$NON-NLS-1$
		}
		return ignored;
	}

	/**
	 * Returns a collection of TocFile that were not processed.
	 */
	protected Collection getContributedTocFiles(String locale) {
		contributingPlugins2IndexPaths = new HashMap();
		Collection contributedTocFiles = new ArrayList();
		Collection ignored = getIgnoredTocs();
		// find extension point
		IExtensionPoint xpt = Platform.getExtensionRegistry()
				.getExtensionPoint(HelpPlugin.PLUGIN_ID, TOC_XP_NAME);
		if (xpt == null)
			return contributedTocFiles;
		// get all extensions
		IExtension[] extensions = xpt.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			String pluginId = extensions[i].getContributor().getName();
			if(!contributingPlugins2IndexPaths.containsKey(pluginId))
				contributingPlugins2IndexPaths.put(pluginId, null);
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length; j++){
				if (configElements[j].getName().equals(TOC_ELEMENT_NAME)) {
					// add to TocFiles declared in this extension
					String href = configElements[j].getAttribute("file"); //$NON-NLS-1$
					String categoryId = configElements[j].getAttribute("category"); //$NON-NLS-1$
					if (href == null
							|| ignored.contains("/" + pluginId + "/" + href) //$NON-NLS-1$ //$NON-NLS-2$
							|| (categoryId != null && ignored.contains(categoryId))) {
						continue;
					}
					boolean isPrimary = "true".equals( //$NON-NLS-1$
							configElements[j].getAttribute("primary")); //$NON-NLS-1$
					String extraDir = configElements[j]
							.getAttribute("extradir"); //$NON-NLS-1$
					contributedTocFiles.add(new TocFile(pluginId, href,
							isPrimary, locale, extraDir, categoryId));
				} else 	if (configElements[j].getName().equals(INDEX_ELEMENT_NAME)) {
					// add to index paths declared in this extension
					String path = configElements[j].getAttribute("path"); //$NON-NLS-1$
					if (path == null
							|| path.length()==0) {
						continue;
					}
					// override entry map entry with new one, only one index path per plugin allowed
					contributingPlugins2IndexPaths.put(pluginId, path);
				} 
			}
		}
		return contributedTocFiles;
	}
	
	/**
	 * Categorizes TOCs by their category ids. If a TOC does not have a category specified,
	 * it is treated as a category of one. The result is a mapping between a string that
	 * represents either the TOC href or the category id, to the Toc or TocCategory,
	 * respectively.
	 * 
	 * @param tocs the collection of Tocs to categorize
	 * @return a mapping of category id/toc href to TocCategory/Toc
	 */
	private Map categorizeTocs(Collection tocs) {
		// guarantees iteration order is same as order of things added
		Map categorized = new LinkedHashMap();
		Iterator iter = tocs.iterator();
		while (iter.hasNext()) {
			Toc toc = (Toc)iter.next();
			String categoryId = toc.getTocFile().getCategoryId();
			if (categoryId != null) {
				// it has a category, add it to the appropriate TocCategory
				TocCategory category = (TocCategory)categorized.get(categoryId);
				if (category == null) {
					// create categories as needed
					category = new TocCategory(categoryId);
					categorized.put(categoryId, category);
				}
				category.add(toc);
			}
			else {
				// doesn't have a category; insert the TOC directly
				categorized.put(toc.getHref(), toc);
			}
		}
		return categorized;
	}
	
	/**
	 * For each item in the list, substitutes it with the value in the map using
	 * the item as a key. If the map does not have the item as a key, the item is
	 * omitted.
	 * 
	 * @param items the items to substitute
	 * @param map the map to use for substitution
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
	
	/**
	 * A category of TOCs. A category has an id and a list of contained TOCs.
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
