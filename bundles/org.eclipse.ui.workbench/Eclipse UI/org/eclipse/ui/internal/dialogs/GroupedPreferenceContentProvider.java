/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The GroupedPreferenceContentProvider is the content provider
 * for showing preferences using groups instead of just categories.
 */
public class GroupedPreferenceContentProvider extends FilteredPreferenceContentProvider {

	Collection groupedIds;

	/**
	 * 
	 */
	public GroupedPreferenceContentProvider() {
		super();
		
		groupedIds = new HashSet();
		WorkbenchPreferenceManager manager =
			(WorkbenchPreferenceManager) WorkbenchPlugin.getDefault().getPreferenceManager();
		WorkbenchPreferenceGroup [] groups = manager.groups;
		
		for (int i = 0; i < groups.length; i++) {
			WorkbenchPreferenceGroup group = groups[i];
			groupedIds.addAll(group.getPageIds());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof WorkbenchPreferenceGroup)
			return ((WorkbenchPreferenceGroup) inputElement).getGroupsAndNodes();
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput == null)
			return;
		
		viewer.refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {

		Object[] children;
		if(parentElement instanceof WorkbenchPreferenceNode)
		  children = super.getChildren(parentElement);
		else
			children = getElements(parentElement);
	
		ArrayList returnValue = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof WorkbenchPreferenceNode) {
				WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) children[i];
				if (!groupedIds.contains(node.getId()))
					returnValue.add(node);
			} else
				returnValue.add(children[i]);

		}
		return returnValue.toArray();
	}
}
