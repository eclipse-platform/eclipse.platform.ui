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

	private boolean fHasChildren = false;
	
	/**
	 * @param contentProvider
	 */
	public HasChildrenUpdate(ModelContentProvider contentProvider, TreePath elementPath, Object element) {
		super(contentProvider, elementPath, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ViewerUpdateMonitor#performUpdate()
	 */
	protected void performUpdate() {
		ModelContentProvider contentProvider = getContentProvider();
		TreePath elementPath = getElementPath();
		if (!fHasChildren) {
			contentProvider.clearFilters(elementPath);
		}
		if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
			System.out.println("setHasChildren(" + getElement() + " >> " + fHasChildren); //$NON-NLS-1$ //$NON-NLS-2$
		}
		((TreeViewer)(contentProvider.getViewer())).setHasChildren(elementPath, fHasChildren);
		if (elementPath.getSegmentCount() > 0) {
			contentProvider.doRestore(elementPath);
		}
	}

	public void setHasChilren(boolean hasChildren) {
		fHasChildren = hasChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#isContained(org.eclipse.jface.viewers.TreePath)
	 */
	boolean isContained(TreePath path) {
		return getElementPath().startsWith(path, null);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("IHasChildrenUpdate: "); //$NON-NLS-1$
		buf.append(getElement());
		return buf.toString();
	}	
}
