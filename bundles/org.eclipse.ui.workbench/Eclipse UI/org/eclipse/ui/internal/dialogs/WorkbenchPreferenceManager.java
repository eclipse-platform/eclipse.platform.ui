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
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;

/**
 * The WorkbenchPreferenceManager is the manager that can handle categories and
 * preference nodes.
 */
public class WorkbenchPreferenceManager extends PreferenceManager {

	WorkbenchPreferenceGroup[] groups;

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
	 * Get a mapping from node id to the group that is looking
	 * for it.
	 * @return Hashtable 
	 */
	private Hashtable getNodeMappings() {
		Hashtable returnValue = new Hashtable();
		for (int i = 0; i < groups.length; i++) {
			addIds(groups[i],returnValue);
		}
		return returnValue;
	}

	/**
	 * Add the page id to group mapping to the return value.
	 * @param group
	 * @param returnValue
	 */
	private void addIds(WorkbenchPreferenceGroup group, Hashtable returnValue) {
		Iterator pageIds = group.getPageIds().iterator();
		while(pageIds.hasNext()){
			returnValue.put(pageIds.next(),group);
		}
		
		Iterator children = group.getChildren().iterator();
		while(children.hasNext()){
			addIds(((WorkbenchPreferenceGroup)children.next()),returnValue);
		}
		
	}

	/**
	 * Sort the supplied categories and make them the categories for the
	 * receiver.
	 * 
	 * @param collectedGroups
	 */
	private void sortGroups(Collection collectedGroups) {

		groups = new WorkbenchPreferenceGroup[collectedGroups.size()];
		collectedGroups.toArray(groups);
		Arrays.sort(groups, new Comparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */
			public int compare(Object arg0, Object arg1) {
				WorkbenchPreferenceGroup first = (WorkbenchPreferenceGroup) arg0;
				WorkbenchPreferenceGroup second = (WorkbenchPreferenceGroup) arg1;

				if (first.getId().equals(GENERAL_ID) || second.getId().equals(ADVANCED_ID))
					return -1;

				if (first.getId().equals(ADVANCED_ID) || second.getId().equals(GENERAL_ID))
					return 1;

				return first.getId().compareTo(second.getId());
			}
		});

	}

	/**
	 * Return the categories of the preference manager.
	 * @return WorkbenchPreferenceGroup[]
	 */
	public WorkbenchPreferenceGroup[] getGroups() {
		return groups;
	}

	/**
	 * Add the pages and the groups to the receiver.
	 * @param pageContributions
	 * @param newGroups
	 */
	public void addPagesAndGroups(Collection pageContributions, Collection newGroups) {

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
		sortGroups(newGroups);

	}
}
