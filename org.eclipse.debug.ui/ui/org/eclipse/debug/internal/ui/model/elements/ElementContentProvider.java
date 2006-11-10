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
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * @since 3.3
 */
public abstract class ElementContentProvider implements IElementContentProvider {
	
	protected static final Object[] EMPTY = new Object[0];

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider#updateChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IElementRequestMonitor)
	 */
	public void update(final IChildrenUpdate update) {
		Job job = new Job("children update") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (!update.isCanceled()) {
					retrieveChildren(update);
				}
				update.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		// TODO: rule
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider#update(org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate)
	 */
	public void update(final IChildrenCountUpdate update) {
		Job job = new Job("child count update") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (!update.isCanceled()) {
					retrieveChildCount(update);
				}
				update.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		// TODO: rule
		job.schedule();
	}
	    
    /**
     * Computes the children for the given parent in the specified context.
     * 
     * @param update update request
     */
    protected void retrieveChildren(IChildrenUpdate update) {
		if (!update.isCanceled()) {
			IStatus status = Status.OK_STATUS;
			try {
				IPresentationContext context = update.getPresentationContext();
				if (supportsContext(context)) {
					int offset = update.getOffset();
					Object parent = update.getElement(update.getParent());
					if (parent == null) {
						update.setCanceled(true);
					} else {
						Object[] children = getChildren(parent, offset, update.getLength(), context, update);
						if (!update.isCanceled() && children != null) {
							for (int i = 0; i < children.length; i++) {
								update.setChild(children[i], offset + i);
							}
						}
					}
				}
			} catch (CoreException e) {
				status = e.getStatus();
			}
			update.setStatus(status);
		}    	
    }
    
    /**
     * Computes whether the given element is a container.
     * 
     * @param parent potential parent
     * @param context presentation context
     * @param monitor result to report to
     */
    protected void retrieveChildCount(IChildrenCountUpdate update) {
		if (!update.isCanceled()) {
			IStatus status = Status.OK_STATUS;
			try {
				IPresentationContext context = update.getPresentationContext();
				TreePath[] parents = update.getParents();
				if (supportsContext(context)) {
					for (int i = 0; i < parents.length; i++) {
						Object parent = update.getElement(parents[i]);
						if (parent == null) {
							// viewer input changed to null
							update.setCanceled(true);
							break;
						}
						int childCount = getChildCount(parent, context, update);
						if (update.isCanceled()) {
							break;
						} else {
							update.setChildCount(parents[i], childCount);
						}
					}
				} else {
					for (int i = 0; i < parents.length; i++) {
						update.setChildCount(parents[i], 0);
					}
				}
			} catch (CoreException e) {
				status = e.getStatus();
			}
			update.setStatus(status);
		}    	
    }    
        
    /**
     * Returns the children for the given parent at the specified index in the specified context
     * or <code>null</code> if none.
     * 
     * @param parent element to retrieve children for
     * @param index child index
     * @param length number of children to retrieve
     * @param context context children will be presented in
     * @return child or <code>null</code>
     * @throws CoreException if an exception occurs retrieving child
     */
    protected abstract Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IProgressMonitor monitor) throws CoreException;
    
    /**
     * Returns the number of children for the given element.
     * 
     * @param element element that may have children
     * @param context context element will be presented in
     * @return number of children
     * @throws CoreException if an exception occurs determining child count
     */
    protected abstract int getChildCount(Object element, IPresentationContext context, IProgressMonitor monitor) throws CoreException;    

    /**
     * Returns whether this adapter supports the given context.
     * 
     * @param context
     * @return whether this adapter supports the given context
     */
    protected boolean supportsContext(IPresentationContext context) {
		return supportsContextId(context.getId());
    }
    
    /**
     * Returns whether this adapter provides content in the specified context id.
     * 
     * @param id part id
     * @return whether this adapter provides content in the specified context id
     */
    protected abstract boolean supportsContextId(String id);	

    /**
     * Returns the element at the given index or <code>null</code> if none.
     * 
     * @param elements
     * @param index
     * @return element or <code>null</code>
     */
    protected Object[] getElements(Object[] elements, int index, int length) {
    	int max = elements.length;
    	if (index < max && ((index + length) > max)) {
    		length = max - index;
    	}
    	if ((index + length) <= elements.length) {
    		Object[] sub = new Object[length];
    		System.arraycopy(elements, index, sub, 0, length);
    		return sub;
    	}
    	return null;
    }

	public void update(final IHasChildrenUpdate update) {
		Job job = new Job("has children update") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (!monitor.isCanceled()) {
					updateHasChildren(update);
				}
				update.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		// TODO: rule
		job.schedule();	
	}

	/**
	 * Updates whether the given elements have children.
	 * 
	 * @param update specifies element and progress monitor
	 */
	protected void updateHasChildren(IHasChildrenUpdate update) {
		if (!update.isCanceled()) {
			IStatus status = Status.OK_STATUS;
			try {
				IPresentationContext context = update.getPresentationContext();
				TreePath[] elements = update.getElements();
				if (supportsContext(context)) {
					for (int i = 0; i < elements.length; i++) {
						Object element = update.getElement(elements[i]);
						boolean hasChildren = hasChildren(element, context, update);
						if (!update.isCanceled()) {
							update.setHasChilren(elements[i], hasChildren);
						}
					}
				} else {
					for (int i = 0; i < elements.length; i++) {
						update.setHasChilren(elements[i], false);
					}
				}
			} catch (CoreException e) {
				status = e.getStatus();
			}
			update.setStatus(status);
			update.done();
		}    	
		
	}

	/**
	 * Returns whether the given element has children in the specified context.
	 * Subclasses can override to be more efficient.
	 * 
	 * @param element
	 * @param context
	 * @param monitor
	 * @return
	 */
	protected boolean hasChildren(Object element, IPresentationContext context, IProgressMonitor monitor) throws CoreException {
		return getChildCount(element, context, monitor) > 0;
	}
    
}
