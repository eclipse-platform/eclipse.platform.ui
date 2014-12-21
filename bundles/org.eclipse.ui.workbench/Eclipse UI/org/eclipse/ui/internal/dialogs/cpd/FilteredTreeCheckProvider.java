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

import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.dialogs.cpd.TreeManager.TreeItem;

/**
 * A check provider which calculates checked state based on leaf states in
 * the tree (as opposed to children in a model).
 *
 * @since 3.5
 */
class FilteredTreeCheckProvider implements ICheckStateProvider {
	private ITreeContentProvider contentProvider;
	private ViewerFilter filter;

	public FilteredTreeCheckProvider(ITreeContentProvider contentProvider,
			ViewerFilter filter) {
		this.contentProvider = contentProvider;
		this.filter = filter;
	}

	@Override
	public boolean isChecked(Object element) {
		TreeItem treeItem = (TreeItem) element;
		return getLeafStates(treeItem, contentProvider, filter) != TreeManager.CHECKSTATE_UNCHECKED;
	}

	@Override
	public boolean isGrayed(Object element) {
		TreeItem treeItem = (TreeItem) element;
		return getLeafStates(treeItem, contentProvider, filter) == TreeManager.CHECKSTATE_GRAY;
	}

	/**
	 * Determines the state <code>item</code> should be (checked, gray or
	 * unchecked) based only on the leafs underneath it (unless it is indeed a
	 * leaf).
	 *
	 * @param item
	 *            the item to find the state of
	 * @param provider
	 *            the content provider which will provide <code>item</code>'s
	 *            children
	 * @param filter
	 *            the filter that will only select elements in the currently
	 *            chosen action set
	 * @return {@link TreeManager#CHECKSTATE_CHECKED},
	 *         {@link TreeManager#CHECKSTATE_GRAY} or
	 *         {@link TreeManager#CHECKSTATE_UNCHECKED}
	 */
	static int getLeafStates(TreeItem item, ITreeContentProvider provider, ViewerFilter filter) {
		Object[] children = provider.getChildren(item);

		boolean checkedFound = false;
		boolean uncheckedFound = false;

		for (Object element : children) {
			if (filter.select(null, null, element)) {
				TreeItem child = (TreeItem) element;
				switch (getLeafStates(child, provider, filter)) {
				case TreeManager.CHECKSTATE_CHECKED: {
					checkedFound = true;
					break;
				}
				case TreeManager.CHECKSTATE_GRAY: {
					checkedFound = uncheckedFound = true;
					break;
				}
				case TreeManager.CHECKSTATE_UNCHECKED: {
					uncheckedFound = true;
					break;
				}
				}
				if (checkedFound && uncheckedFound) {
					return TreeManager.CHECKSTATE_GRAY;
				}
			}
		}

		if (!checkedFound && !uncheckedFound) {
			return item.getState() ? TreeManager.CHECKSTATE_CHECKED : TreeManager.CHECKSTATE_UNCHECKED;
		}
		return checkedFound ? TreeManager.CHECKSTATE_CHECKED : TreeManager.CHECKSTATE_UNCHECKED;
	}
}