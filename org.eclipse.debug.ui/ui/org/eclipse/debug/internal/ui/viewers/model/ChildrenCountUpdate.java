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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.3
 */
class ChildrenCountUpdate extends ViewerUpdateMonitor implements IChildrenCountUpdate {

	/**
	 * Map of <code>TreePath</code>s to <code>Integer</code>s.
	 */
	private Map fCounts = new HashMap();
	
	/**
	 * Set of <code>TreePath</code>s.
	 */
	private Set fParents = new HashSet();
	
	private boolean fStarted = false;
	private IElementContentProvider fContentProvider;
	
	/**
	 * @param contentProvider
	 */
	public ChildrenCountUpdate(ModelContentProvider contentProvider, IElementContentProvider elementContentProvider) {
		super(contentProvider);
		fContentProvider = elementContentProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ViewerUpdateMonitor#performUpdate()
	 */
	protected void performUpdate() {
		Iterator iterator = fCounts.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry) iterator.next();
			int count = ((Integer)(entry.getValue())).intValue();
			TreePath parentPath = (TreePath) entry.getKey();
			int viewCount = count;
			if (count == 0) {
				getContentProvider().clearFilters(parentPath);
			} else {
				viewCount = getContentProvider().modelToViewChildCount(parentPath, count);
			}
			if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
				System.out.println("setChildCount(" + getElement(parentPath) + ", modelCount: " + count + " viewCount: " + viewCount + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			((TreeViewer)(getContentProvider().getViewer())).setChildCount(parentPath, viewCount);
		}
	}

	public void setChildCount(TreePath parentPath, int numChildren) {
		fCounts.put(parentPath, new Integer(numChildren));
	}

	/**
	 * @param element
	 * @return
	 */
	protected boolean coalesce(TreePath treePath) {
		fParents.add(treePath);
		return true;
	}

	/**
	 * 
	 */
	protected void start() {
		synchronized (this) {
			if (fStarted) {
				return;
			}
			fStarted = true;
		}
		TreeModelContentProvider contentProvider = (TreeModelContentProvider)getContentProvider();
		contentProvider.countRequestStarted(fContentProvider);
		if (!isCanceled()) {
			fContentProvider.update(this);
		} else {
			done();
		}
	}

	public TreePath[] getParents() {
		return (TreePath[]) fParents.toArray(new TreePath[fParents.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#isContained(org.eclipse.jface.viewers.TreePath)
	 */
	boolean isContained(TreePath path) {
		return ((TreePath)fParents.iterator().next()).startsWith(path, null);
	}

}
