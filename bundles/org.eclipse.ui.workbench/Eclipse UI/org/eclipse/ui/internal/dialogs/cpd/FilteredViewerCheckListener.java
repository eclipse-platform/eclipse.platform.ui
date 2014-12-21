/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs.cpd;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DisplayItem;

/**
 * A check listener to bring about the expected change in a model based on a
 * check event in a filtered viewer. Since the checked state of a parent in
 * a filtered viewer is not based on its model state, but rather its leafs'
 * states, when a non-leaf element's check state changes, its model state
 * does not necessarily change, but its leafs' model states do.
 *
 * @since 3.5
 */
class FilteredViewerCheckListener implements ICheckStateListener {
	private ITreeContentProvider contentProvider;
	private ViewerFilter filter;

	public FilteredViewerCheckListener(
			ITreeContentProvider contentProvider, ViewerFilter filter) {
		this.contentProvider = contentProvider;
		this.filter = filter;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		setAllLeafs((DisplayItem) event.getElement(), event
				.getChecked(), contentProvider, filter);
	}

	/**
	 * Sets all leafs under a {@link DisplayItem} to either visible or
	 * invisible. This is for use with the action set trees, where the only
	 * state used is that of leafs, and the rest is rolled up to the parents.
	 * Thus, this method effectively sets the state of the entire branch.
	 *
	 * @param item
	 *            the item whose leafs underneath (or itself, if it is a leaf)
	 *            to <code>value</code>
	 * @param value
	 *            <code>true</code>for visible, <code>false</code> for invisible
	 * @param provider
	 *            the content provider which will provide <code>item</code>'s
	 *            children
	 * @param filter
	 *            the filter that will only select elements in the currently
	 *            chosen action set
	 */
	static void setAllLeafs(DisplayItem item, boolean value, ITreeContentProvider provider, ViewerFilter filter) {
		Object[] children = provider.getChildren(item);
		boolean isLeaf = true;

		for (Object element : children) {
			isLeaf = false;
			if (filter.select(null, null, element)) {
				DisplayItem child = (DisplayItem) element;
				setAllLeafs(child, value, provider, filter);
			}
		}

		if (isLeaf) {
			item.setCheckState(value);
		}
	}
}