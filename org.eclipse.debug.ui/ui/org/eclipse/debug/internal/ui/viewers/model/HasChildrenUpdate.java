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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.3
 */
class HasChildrenUpdate extends ViewerUpdateMonitor implements IHasChildrenUpdate {

	/**
	 * Map of <code>TreePath</code>s to <code>Boolean</code>s.
	 */
	private Map fBooleans = new HashMap();
	
	/**
	 * Set of <code>TreePath</code>s.
	 */
	private Set fElements = new HashSet();
	
	private boolean fStarted = false;
	private IElementContentProvider fContentProvider;
	
	/**
	 * @param contentProvider
	 */
	public HasChildrenUpdate(ModelContentProvider contentProvider, IElementContentProvider elementContentProvider) {
		super(contentProvider);
		fContentProvider = elementContentProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ViewerUpdateMonitor#performUpdate()
	 */
	protected void performUpdate() {
		Iterator iterator = fBooleans.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry) iterator.next();
			boolean hasChildren = ((Boolean)(entry.getValue())).booleanValue();
			TreePath elementPath = (TreePath) entry.getKey();
			ModelContentProvider contentProvider = getContentProvider();
			if (!hasChildren) {
				contentProvider.clearFilters(elementPath);
			}
			if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
				System.out.println("setHasChildren(" + getElement(elementPath) + " >> " + hasChildren); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (elementPath.getSegmentCount() > 0) {
				((TreeViewer)(contentProvider.getViewer())).setHasChildren(elementPath, hasChildren);
				contentProvider.doRestore(elementPath);
			} else {
				((TreeViewer)(contentProvider.getViewer())).setHasChildren(getElement(elementPath), hasChildren);
			}
			
		}
	}

	/**
	 * @param element
	 * @return
	 */
	protected boolean coalesce(TreePath treePath) {
		fElements.add(treePath);
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
		contentProvider.hasChildrenRequestStarted(fContentProvider);
		if (!isCanceled()) {
			fContentProvider.update(this);
		} else {
			done();
		}
	}

	public TreePath[] getElements() {
		return (TreePath[]) fElements.toArray(new TreePath[fElements.size()]);
	}

	public void setHasChilren(TreePath element, boolean hasChildren) {
		fBooleans.put(element, Boolean.valueOf(hasChildren));
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#isContained(org.eclipse.jface.viewers.TreePath)
	 */
	boolean isContained(TreePath path) {
		return ((TreePath)fElements.iterator().next()).startsWith(path, null);
	}

}
