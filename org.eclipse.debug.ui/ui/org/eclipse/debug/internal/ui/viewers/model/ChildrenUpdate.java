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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.jface.viewers.TreePath;

/**
 * @since 3.3 
 */
class ChildrenUpdate extends ViewerUpdateMonitor implements IChildrenUpdate {
	
	private TreePath fParentPath;
	private Object[] fElements;
	private int fIndex;
	private int fLength;
	private IElementContentProvider fContentProvider;
	private boolean fStarted = false;

	/**
	 * Constructs a request to update an element
	 * 
	 * @param node node to update
	 * @param model model containing the node
	 */
	public ChildrenUpdate(ModelContentProvider provider, TreePath parentPath, int index, IElementContentProvider presentation) {
		super(provider);
		fParentPath = parentPath;
		fIndex = index;
		fLength = 1;
		fContentProvider = presentation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		TreeModelContentProvider provider = (TreeModelContentProvider) getContentProvider();
		if (fElements != null) {
			InternalTreeModelViewer viewer = (InternalTreeModelViewer) provider.getViewer();
			for (int i = 0; i < fElements.length; i++) {
				int modelIndex = fIndex + i;
				Object element = fElements[i];
				if (element != null) {
					int viewIndex = provider.modelToViewIndex(fParentPath, modelIndex);
					if (provider.shouldFilter(fParentPath, element)) {
						if (provider.addFilteredIndex(fParentPath, modelIndex)) {
							if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
								System.out.println("REMOVE(" + getElement(fParentPath) + ", modelIndex: " + modelIndex + " viewIndex: " + viewIndex + ", " + element + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							}
							viewer.remove(fParentPath, viewIndex);
						}
					} else {
						if (provider.isFiltered(fParentPath, modelIndex)) {
							provider.clearFilteredChild(fParentPath, modelIndex);
							int insertIndex = provider.modelToViewIndex(fParentPath, modelIndex);
							if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
								System.out.println("insert(" + fParentPath.getLastSegment() + ", modelIndex: " + modelIndex + " insertIndex: " + insertIndex + ", " + element + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							}
							if (fParentPath.getSegmentCount() == 0) {
								// TODO: does empty path work in viewer?
								viewer.insert(getElement(fParentPath), element, insertIndex);
							} else {
								viewer.insert(fParentPath, element, insertIndex);
							}
						} else {
							if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
								System.out.println("replace(" + getElement(fParentPath) + ", modelIndex: " + modelIndex + " viewIndex: " + viewIndex + ", " + element + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							}
							if (fParentPath.getSegmentCount() > 0) {
								viewer.replace(fParentPath, viewIndex, element);
							} else {
								viewer.replace(getElement(fParentPath), viewIndex, element);
							}
						}
						TreePath childPath = fParentPath.createChildPath(element);
						provider.updateHasChildren(childPath);
					}
				}
			}
		} else {
			provider.updateHasChildren(fParentPath);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#setChild(java.lang.Object, int)
	 */
	public void setChild(Object child, int index) {
		if (fElements == null) {
			fElements = new Object[fLength];
		}
		fElements[index - fIndex] = child;
	}
	
	/**
	 * Coalesce the request with the given index. Return whether the requests can be 
	 * coalesced.
	 * 
	 * @param index
	 * @return whether it worked
	 */
	public boolean coalesce(int index) {
		if (index == fIndex + fLength) {
			fLength++;
			return true;
		}
		return false;
	}
	
	public void start() {
		synchronized (this) {
			if (fStarted) {
				return;
			}
			fStarted = true;
		}
		//System.out.println("\tRequest (" + fParent + "): " + fIndex + " length: " + fLength);
		TreeModelContentProvider contentProvider = (TreeModelContentProvider)getContentProvider();
		contentProvider.childRequestStarted(this);
		if (!isCanceled()) {
			fContentProvider.update(this);
		} else {
			done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#getOffset()
	 */
	public int getOffset() {
		return fIndex;
	}

	public TreePath getParent() {
		return fParentPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#isContained(org.eclipse.jface.viewers.TreePath)
	 */
	boolean isContained(TreePath path) {
		return fParentPath.startsWith(path, null);
	}
	
}
