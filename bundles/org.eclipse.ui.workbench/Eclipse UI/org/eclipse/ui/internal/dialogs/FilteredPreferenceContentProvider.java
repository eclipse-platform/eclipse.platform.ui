/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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

import org.eclipse.jface.preference.PreferenceContentProvider;

import org.eclipse.ui.activities.WorkbenchActivityHelper;

import org.eclipse.ui.internal.activities.ws.FilterableObject;

/**
 * Adds filtering support to <code>PreferenceContentProvider</code>.
 * 
 * @since 3.0
 */
public class FilteredPreferenceContentProvider extends PreferenceContentProvider {

	/**
	 * Filtering support.
	 */
	private FilterableObject filterableObject;

	/**
	 * Create a new instance of the <code>FilteringPreferenceContentProvider</code>.
	 * 
	 * @param filtering
	 *            the initial filtering state.
	 */
	public FilteredPreferenceContentProvider(boolean filtering) {
		filterableObject = new FilterableObject(filtering);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		Object[] children = super.getChildren(parentElement);
		if (!getFiltering())
			return children;

		ArrayList filteredChildren = new ArrayList(children.length);
		for (int i = 0; i < children.length; i++) {
			if (WorkbenchActivityHelper.filterItem(children[i]))
				continue;

			filteredChildren.add(children[i]);
		}
		return filteredChildren.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.activities.ws.FilterableObject#getFiltering()
	 */
	public boolean getFiltering() {
		return filterableObject.getFiltering();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		Object parent = super.getParent(element);
		if (WorkbenchActivityHelper.filterItem(parent))
			return null;
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.activities.ws.FilterableObject#setFiltering(boolean)
	 */
	public void setFiltering(boolean filtering) {
		filterableObject.setFiltering(filtering);
	}

}
