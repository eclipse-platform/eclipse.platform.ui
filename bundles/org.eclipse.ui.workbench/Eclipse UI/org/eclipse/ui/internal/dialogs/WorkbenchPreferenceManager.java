/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;

/**
 * The WorkbenchPreferenceManager is the manager that can handle categories and
 * preference nodes.
 */
public class WorkbenchPreferenceManager extends PreferenceManager {

	WorkbenchPreferenceCategory[] categories;

	private final static String GENERAL_ID = "org.eclipse.ui.general";//$NON-NLS-1$

	private final static String ADVANCED_ID = "org.eclipse.ui.advanced";//$NON-NLS-1$

	/**
	 * Create a new instance of the receiver with the specified seperatorChar
	 * 
	 * @param separatorChar
	 */
	public WorkbenchPreferenceManager(char separatorChar) {
		super(separatorChar);
	}

	/**
	 * Add the contributions to the manager.
	 * 
	 * @param pageContributions
	 */
	public void addContributions(List pageContributions) {

		ArrayList collectedCategories = new ArrayList();
		// Add the contributions to the manager
		Iterator iterator = pageContributions.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			if (next instanceof IPreferenceNode)
				addToRoot((IPreferenceNode) next);
			else
				collectedCategories.add(next);
		}
		sortCategories(collectedCategories);
		categorizePages();
	}

	/**
	 * Categorize all pages into thier specified categories
	 */
	private void categorizePages() {
		
		if(categories.length == 0)
			return;
		
		IPreferenceNode[] nodes = getRoot().getSubNodes();
		Hashtable categoryTable = new Hashtable();
		for (int i = 0; i < categories.length; i++) {
			categoryTable.put(categories[i].getId(), categories[i]);
		}

		WorkbenchPreferenceCategory advanced = categories[categories.length - 1];
		for (int i = 0; i < nodes.length; i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) nodes[i];
			if (node.getCategory() == null)
				advanced.addNode(node);
			else {
				if (categoryTable.contains(node.getCategory()))
					((WorkbenchPreferenceCategory) categoryTable.get(node
							.getCategory())).addNode(node);
			}
		}

	}

	/**
	 * Sort the supplied categories and make them the categories for the
	 * receiver.
	 * 
	 * @param collectedCategories
	 */
	private void sortCategories(ArrayList collectedCategories) {

		categories = new WorkbenchPreferenceCategory[collectedCategories.size()];
		collectedCategories.toArray(categories);
		Arrays.sort(categories, new Comparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */
			public int compare(Object arg0, Object arg1) {
				WorkbenchPreferenceCategory first = (WorkbenchPreferenceCategory) arg0;
				WorkbenchPreferenceCategory second = (WorkbenchPreferenceCategory) arg1;

				if (first.getId().equals(GENERAL_ID)
						|| second.getId().equals(ADVANCED_ID))
					return -1;

				if (first.getId().equals(ADVANCED_ID)
						|| second.getId().equals(GENERAL_ID))
					return 1;

				return first.getId().compareTo(second.getId());
			}
		});

	}

	/**
	 * Return the categories of the preference manager.
	 * @return WorkbenchPreferenceCategory[]
	 */
	public WorkbenchPreferenceCategory[] getCategories() {
		return categories;
	}
}
