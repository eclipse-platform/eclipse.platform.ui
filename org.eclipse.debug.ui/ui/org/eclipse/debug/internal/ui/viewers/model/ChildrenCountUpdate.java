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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.3
 */
class ChildrenCountUpdate extends ViewerUpdateMonitor implements IChildrenCountUpdate {

	private int fCount = 0;
	
	/**
	 * @param contentProvider
	 */
	public ChildrenCountUpdate(ModelContentProvider contentProvider, TreePath elementPath, Object element) {
		super(contentProvider, elementPath, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ViewerUpdateMonitor#performUpdate()
	 */
	protected void performUpdate() {
		int viewCount = fCount;
		TreePath elementPath = getElementPath();
		if (viewCount == 0) {
			getContentProvider().clearFilters(elementPath);
		} else {
			getContentProvider().setModelChildCount(elementPath, fCount);
			viewCount = getContentProvider().modelToViewChildCount(elementPath, fCount);
		}
		if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
			System.out.println("setChildCount(" + getElement() + ", modelCount: " + fCount + " viewCount: " + viewCount + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		((TreeViewer)(getContentProvider().getViewer())).setChildCount(elementPath, viewCount);
	}

	public void setChildCount(int numChildren) {
		fCount = numChildren;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#isContained(org.eclipse.jface.viewers.TreePath)
	 */
	boolean isContained(TreePath path) {
		return getElementPath().startsWith(path, null);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("IChildrenCountUpdate: "); //$NON-NLS-1$
		buf.append(getElement());
		return buf.toString();
	}	

}
