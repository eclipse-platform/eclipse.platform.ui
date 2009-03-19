/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.help.ITocContribution;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.util.ProductPreferences;

public class TocSorter {
	
	/*
	 * A category of tocs. A category has an id and a list of contained
	 * tocs.
	 */
	private static class TocCategory extends ArrayList {
		
		private static final long serialVersionUID = 1L;
		
		/**
		 * Constructs a new empty TOC category with the given id.
		 * 
		 * @param id the category's id
		 */
		public TocCategory(String id) {
		}
		
	}

	/*
	 * Orders the given toc contributions by category and product preference.
	 */
	public ITocContribution[] orderTocContributions(ITocContribution[] unorderedTocs) {
		// first categorize the TOCs
		List itemsToOrder = new ArrayList();
		Map categorized = categorizeTocs(Arrays.asList(unorderedTocs), itemsToOrder);
		Map nameIdMap = createNameIdMap(categorized);
			
		// order them
		List orderedItems = ProductPreferences.getTocOrder(itemsToOrder, nameIdMap);
			
		// replace with actual TocContribution or category
		orderedItems = substituteValues(orderedItems, categorized);
			
		// expand the categories
		orderedItems = expandCategories(orderedItems);
		return (ITocContribution[])orderedItems.toArray(new ITocContribution[orderedItems.size()]);
	}
	
	// Create a mapping from an id to a label that can be sorted
	private Map createNameIdMap(Map categorized) { 
		Map map = new HashMap();
		for (Iterator iter = categorized.keySet().iterator(); iter.hasNext();) {
			String key = (String)iter.next();
			Object value = categorized.get(key);
			ITocContribution toc;
			if (value instanceof TocCategory) {
				TocCategory category = (TocCategory)value;
				toc = (ITocContribution) category.get(0);
			} else {
				toc = (ITocContribution)value;
			}
			map.put(key, toc.getToc().getLabel());
		}
	    return map;
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
			if (categoryId != null && categoryId.length() > 0) {
				// it has a category, add it to the appropriate TocCategory
				TocCategory category = (TocCategory)categorized.get(categoryId);
				if (category == null) {
					// create categories as needed
					category = new TocCategory(categoryId);
					categorized.put(categoryId, category);
					tocOrder.add(categoryId);
					category.add(toc);
				} else {
					// Add in alphabetic sequence
					String tocLabel = toc.getToc().getLabel();
					boolean done = false;
					for (int next = 0; next < category.size() && !done; next++ ) {
						String nextName = ((ITocContribution)category.get(next)).getToc().getLabel();
						if (tocLabel.compareToIgnoreCase(nextName) < 0) {
							done = true;
							category.add(next, toc);
						}
					}
					if (!done) {
						category.add(toc);
					}
				}
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

}
