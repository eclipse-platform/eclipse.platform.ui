/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.3
 */
class ChildrenCountUpdate extends ViewerUpdateMonitor implements IChildrenCountUpdate {

	private int fCount = 0;
	
	private List fBatchedRequests = null;
	
	/**
	 * @param contentProvider
	 */
	public ChildrenCountUpdate(ModelContentProvider contentProvider, TreePath elementPath, Object element, IElementContentProvider elementContentProvider, IPresentationContext context) {
		super(contentProvider, elementPath, element, elementContentProvider, context);
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
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("IChildrenCountUpdate: "); //$NON-NLS-1$
		buf.append(getElement());
		return buf.toString();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#coalesce(org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor)
	 */
	boolean coalesce(ViewerUpdateMonitor request) {
		if (request instanceof ChildrenCountUpdate) {
			if (getElementPath().equals(request.getElementPath())) {
				// duplicate request
				return true;
			} else if (getElementContentProvider().equals(request.getElementContentProvider())) {
				if (fBatchedRequests == null) {
					fBatchedRequests = new ArrayList();
					fBatchedRequests.add(this);
				}
				fBatchedRequests.add(request);
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#startRequest()
	 */
	void startRequest() {
		if (fBatchedRequests == null) {
			getElementContentProvider().update(new IChildrenCountUpdate[]{this});
		} else {
			getElementContentProvider().update((IChildrenCountUpdate[]) fBatchedRequests.toArray(new IChildrenCountUpdate[fBatchedRequests.size()]));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#getPriority()
	 */
	int getPriority() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#getSchedulingPath()
	 */
	TreePath getSchedulingPath() {
		TreePath path = getElementPath();
		if (path.getSegmentCount() > 0) {
			return path.getParentPath();
		}
		return path;
	}		
}
