/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Abstract implementation of an asynchronous tree contenxt adapter.
 * <p>
 * Clients may subclass this class.
 * </p>
 * @since 3.2
 */
public abstract class AsynchronousTreeContentAdapter implements IAsynchronousTreeContentAdapter {
	
	protected static final Object[] EMPTY = new Object[0];
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IAsynchronousTreeContentAdapter#retrieveChildren(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext, org.eclipse.debug.ui.viewers.IChildrenRequestMonitor)
     */
    public void retrieveChildren(final Object parent, final IPresentationContext context, final IChildrenRequestMonitor result) {
		Job job = new Job("Retrieving Children") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (!monitor.isCanceled()) {
					computeChildren(parent, context, result);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IAsynchronousTreeContentAdapter#isContainer(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext, org.eclipse.debug.ui.viewers.IContainerRequestMonitor)
     */
    public void isContainer(final Object element, final IPresentationContext context, final IContainerRequestMonitor result) {
		Job job = new Job("Computing hasChildren") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				if (!monitor.isCanceled()) {
					computeIsContainer(element, context, result);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
    
    /**
     * Computes the children for the given parent in the specified context.
     * 
     * @param parent parent to retrieve children for
     * @param context presentation contenxt
     * @param monitor result to report to
     */
    protected void computeChildren(Object parent, IPresentationContext context, IChildrenRequestMonitor monitor) {
		if (!monitor.isCanceled()) {
			IStatus status = Status.OK_STATUS;
			try {
				if (supportsContext(context)) {
					monitor.addChildren(getChildren(parent, context));
				}
			} catch (CoreException e) {
				status = e.getStatus();
			}
			monitor.setStatus(status);
			monitor.done();
		}    	
    }
    
    /**
     * Computes whether the given element is a container.
     * 
     * @param parent potential parent
     * @param context presentation contenxt
     * @param monitor result to report to
     */
    protected void computeIsContainer(Object parent, IPresentationContext context, IContainerRequestMonitor monitor) {
		if (!monitor.isCanceled()) {
			IStatus status = Status.OK_STATUS;
			try {
				monitor.setIsContainer(hasChildren(parent, context));
			} catch (CoreException e) {
				status = e.getStatus();
			}
			monitor.setStatus(status);
			monitor.done();
		}    	
    }    
        
    /**
     * Returns the children for the given parent in the specified context.
     * 
     * @param parent element to retrieve children for
     * @param context context children will be presented in
     * @return children
     * @throws CoreException if an exception occurrs retieving children
     */
    protected abstract Object[] getChildren(Object parent, IPresentationContext context) throws CoreException;
    
    /**
     * Returns whether the given element has children in the specified context.
     * 
     * @param element element that may have children
     * @param context context element will be presented in
     * @return whether the given element has children in the specified context
     * @throws CoreException if an exception occurrs determining whether the
     *  element has children
     */
    protected abstract boolean hasChildren(Object element, IPresentationContext context) throws CoreException;    

    /**
     * Returns whether this adapter supports the given context.
     * 
     * @param context
     * @return whether this adapter supports the given context
     */
    protected boolean supportsContext(IPresentationContext context) {
		IWorkbenchPart part = context.getPart();
		if (part != null) {
			return supportsPartId(part.getSite().getId());
		}
		return true;    	
    }
    
    /**
     * Returns whether this adapter provides content in the specified part.
     * 
     * @param id part id
     * @return whether this adapter provides content in the specified part
     */
    protected abstract boolean supportsPartId(String id);
}
