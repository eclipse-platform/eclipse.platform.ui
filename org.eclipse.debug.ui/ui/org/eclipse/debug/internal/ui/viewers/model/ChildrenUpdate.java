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
	
	private Object[] fElements;
	private int fIndex;
	private int fLength;

	/**
	 * Constructs a request to update an element
	 * 
	 * @param node node to update
	 * @param model model containing the node
	 */
	public ChildrenUpdate(ModelContentProvider provider, TreePath elementPath, Object element, int index, IElementContentProvider elementContentProvider) {
		super(provider, elementPath, element, elementContentProvider);
		fIndex = index;
		fLength = 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		TreeModelContentProvider provider = (TreeModelContentProvider) getContentProvider();
		TreePath elementPath = getElementPath();
		if (fElements != null) {
			InternalTreeModelViewer viewer = (InternalTreeModelViewer) provider.getViewer();
			for (int i = 0; i < fElements.length; i++) {
				int modelIndex = fIndex + i;
				Object element = fElements[i];
				if (element != null) {
					int viewIndex = provider.modelToViewIndex(elementPath, modelIndex);
					if (provider.shouldFilter(elementPath, element)) {
						if (provider.addFilteredIndex(elementPath, modelIndex, element)) {
							if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
								System.out.println("REMOVE(" + getElement() + ", modelIndex: " + modelIndex + " viewIndex: " + viewIndex + ", " + element + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							}
							viewer.remove(elementPath, viewIndex);
						}
					} else {
						if (provider.isFiltered(elementPath, modelIndex)) {
							provider.clearFilteredChild(elementPath, modelIndex);
							int insertIndex = provider.modelToViewIndex(elementPath, modelIndex);
							if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
								System.out.println("insert(" + getElement() + ", modelIndex: " + modelIndex + " insertIndex: " + insertIndex + ", " + element + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							}
							viewer.insert(elementPath, element, insertIndex);
						} else {
							if (ModelContentProvider.DEBUG_CONTENT_PROVIDER) {
								System.out.println("replace(" + getElement() + ", modelIndex: " + modelIndex + " viewIndex: " + viewIndex + ", " + element + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							}
							viewer.replace(elementPath, viewIndex, element);
						}
						TreePath childPath = elementPath.createChildPath(element);
						provider.updateHasChildren(childPath);
					}
				}
			}
		} else {
			provider.updateHasChildren(elementPath);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#coalesce(org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor)
	 */
	boolean coalesce(ViewerUpdateMonitor request) {
		if (request instanceof ChildrenUpdate) {
			ChildrenUpdate cu = (ChildrenUpdate) request;
			int end = fIndex + fLength;
			int otherStart = cu.getOffset();
			if (otherStart == end) {
				fLength = fLength + cu.getLength();
				return true;
			} else if (otherStart == fIndex) {
				if (cu.getLength() > getLength()) {
					fLength = cu.getLength();
				}
				return true;
			} else if ((otherStart > fIndex) && (otherStart < end)) {
				int otherEnd = otherStart + cu.getLength();
				fLength = otherEnd - fIndex;
				return true;
			}
		}
		return false;
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#startRequest()
	 */
	void startRequest() {
		getElementContentProvider().update(new IChildrenUpdate[]{this});
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("IChildrenUpdate: "); //$NON-NLS-1$
		buf.append(getElement());
		buf.append(" {"); //$NON-NLS-1$
		buf.append(getOffset());
		buf.append(',');
		buf.append(getLength());
		buf.append("}"); //$NON-NLS-1$
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#getPriority()
	 */
	int getPriority() {
		return 3;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.ViewerUpdateMonitor#getSchedulingPath()
	 */
	TreePath getSchedulingPath() {
		return getElementPath();
	}		
}

