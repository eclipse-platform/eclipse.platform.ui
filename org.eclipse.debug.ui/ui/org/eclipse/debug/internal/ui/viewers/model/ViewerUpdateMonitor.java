/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech (Wind River) - added support for a virtual tree model viewer (Bug 242489)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.internal.core.commands.Request;
import org.eclipse.debug.internal.ui.viewers.AsynchronousSchedulingRuleFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @since 3.3
 */
public abstract class ViewerUpdateMonitor extends Request implements IViewerUpdate {

	private ModelContentProvider fContentProvider;
	
	/**
	 * Element's tree path
	 */
	private TreePath fElementPath;
	
	/**
	 * Element
	 */
	private Object fElement;
	
	/**
	 * Element content provider
	 */
	private IElementContentProvider fElementContentProvider;
    
    /**
     * Whether this request's 'done' method has been called.
     */
    private boolean fDone = false;
    
    /**
     * Whether this request has been started
     */
    private boolean fStarted = false;
    
    /**
     * Viewer input at the time the request was made
     */
    private Object fViewerInput = null;
    
    /**
     * Whether this update has been delegated to another content provider
     * @since 3.4
     */
    private boolean fIsDelegated = false;
    
    /**
     * Presentation context
     */
    private IPresentationContext fContext;

    
    protected WorkbenchJob fViewerUpdateJob;
    
    /**
     * Constructs an update for the given content provider
     * 
     * @param contentProvider content provider
     * @param elementPath path to associated model element - empty for root element
     * @param element associated model element
     */
    public ViewerUpdateMonitor(ModelContentProvider contentProvider, Object viewerInput, TreePath elementPath, Object element, IElementContentProvider elementContentProvider, IPresentationContext context) {
    	fContext = context;
    	fViewerInput = viewerInput;
    	fElementContentProvider = elementContentProvider;
        fContentProvider = contentProvider;
        fElement = element;
        fElementPath = elementPath;
        // serialize updates per viewer
        fViewerUpdateJob =  new WorkbenchJob(contentProvider.getViewer().getDisplay(), "Asynchronous viewer update") { //$NON-NLS-1$
            public IStatus runInUIThread(IProgressMonitor monitor) {
                // necessary to check if viewer is disposed
                try {
                    if (!isCanceled() && !getContentProvider().isDisposed()) {
                        IStatus status = getStatus();
                        if (status == null || status.isOK()) {
                            performUpdate();
                        }
                    }
                } finally {
                    getContentProvider().updateComplete(ViewerUpdateMonitor.this);
                }
                return Status.OK_STATUS;
            }
        };
        fViewerUpdateJob.setRule(getUpdateSchedulingRule());
        fViewerUpdateJob.setSystem(true);
    }
    
    /**
     * Returns the scheduling rule for viewer update job.
     * 
     * @return rule or <code>null</code>
     */
    protected ISchedulingRule getUpdateSchedulingRule() {
    	return AsynchronousSchedulingRuleFactory.getDefault().newSerialPerObjectRule(getContentProvider());
    }
    
    /**
     * Returns the model content provider this update is being performed for.
     * 
     * @return the model content provider this update is being performed for
     */
    protected ModelContentProvider getContentProvider() {
        return fContentProvider;
    }   
    
    /**
     * Returns the element content provider to use for this request
     * 
     * @return element content provider
     */
    protected IElementContentProvider getElementContentProvider() {
    	return fElementContentProvider;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    public final void done() {
    	synchronized (this) {
    		if (isDone()) {
    			return;
    		}
    		fDone = true;
		}
    	scheduleViewerUpdate();
	}
    
    /**
     * Returns whether this request is done yet.
     * 
     * @return
     */
    protected synchronized boolean isDone() {
    	return fDone;
    }

    protected void scheduleViewerUpdate() {
        if(!isCanceled()) {
            fViewerUpdateJob.schedule();
        } else {
        	getContentProvider().updateComplete(this);
        }
    }
    
    /**
	 * Notification this update has been completed and should now be applied to
	 * this update's viewer. This method is called in the UI thread.
	 */
    protected abstract void performUpdate();
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getPresentationContext()
	 */
	public IPresentationContext getPresentationContext() {
		return fContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElement()
	 */
	public Object getElement() {
		return fElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElementPath()
	 */
	public TreePath getElementPath() {
		return fElementPath;
	}
	
	/**
	 * Returns whether this request can coalesce the given request, and performs the
	 * coalesce if it can.
	 * 
	 * @param update request to coalesce with this request
	 * @return whether it worked
	 */
	abstract boolean coalesce(ViewerUpdateMonitor update);

	/**
	 * Returns whether this update or any coalesced updates is for an 
	 * element at the given path.
     * @since 3.6
	 */
	abstract boolean containsUpdate(TreePath path);
	
	/**
	 * Starts this request. Subclasses must override startRequest().
	 */
	final void start() {
		synchronized (this) {
			if (fStarted) {
				return;
			}
			fStarted = true;
		}
		getContentProvider().updateStarted(this);
		if (!isCanceled()) {
			startRequest();
		} else {
			done();
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getViewerInput()
	 */
	public Object getViewerInput() {
		return fViewerInput;
	}

	/**
	 * Subclasses must override to initiate specific request types.
	 */
	abstract void startRequest();
	
	/**
	 * Returns the priority of this request. Subclasses must override. The
	 * highest priority is 1. Priorities indicate the order that waiting
	 * requests should be started in (for example, 'hasChildren' before 'update child count'). 
	 * 
	 * @return priority
	 */
	abstract int getPriority();
	
	/**
	 * Returns a path used to schedule this request - i.e. based on this path, this
	 * request will be scheduled to run when no requests are running against the
	 * same element or a parent of the element denoted by the path.
	 * 
	 * @return path used to schedule request
	 */
	abstract TreePath getSchedulingPath();

	/**
	 * Sets whether this update has been delegated to another content provider
	 * @param delegated whether the update has been delegated
	 * @since 3.4
	 */
	public void setDelegated(boolean delegated) {
		fIsDelegated = delegated;
	}

	/**
	 * @return whether this update has been delegated to another content provider
	 * @since 3.4
	 */
	public boolean isDelegated() {
		return fIsDelegated;
	}
}
