/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.3
 */
class HasChildrenUpdate extends ViewerUpdateMonitor implements IHasChildrenUpdate {

	private TreePath fElementPath;
	private boolean fHasChildren = false;
	
	/**
	 * @param contentProvider
	 */
	public HasChildrenUpdate(ModelContentProvider contentProvider, TreePath elementPath) {
		super(contentProvider);
		fElementPath = elementPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ViewerUpdateMonitor#performUpdate()
	 */
	protected void performUpdate() {
		ModelContentProvider contentProvider = getContentProvider();
		if (!fHasChildren) {
			contentProvider.clearFilters(fElementPath);
		}
		if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
			System.out.println("setHasChildren(" + getElement(fElementPath) + " >> " + fHasChildren); //$NON-NLS-1$ //$NON-NLS-2$
		}
		((TreeViewer)(contentProvider.getViewer())).setHasChildren(fElementPath, fHasChildren);
		if (fElementPath.getSegmentCount() > 0) {
			contentProvider.doRestore(fElementPath);
		}
	}

	public TreePath getElementPath() {
		return fElementPath;
	}

	public void setHasChilren(boolean hasChildren) {
		fHasChildren = hasChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#isContained(org.eclipse.jface.viewers.TreePath)
	 */
	boolean isContained(TreePath path) {
		return fElementPath.startsWith(path, null);
	}

}
