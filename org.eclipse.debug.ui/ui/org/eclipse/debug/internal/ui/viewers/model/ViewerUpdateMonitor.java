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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.internal.ui.commands.actions.AbstractRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.AsynchronousSchedulingRuleFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @since 3.3
 */
abstract class ViewerUpdateMonitor extends AbstractRequestMonitor implements IViewerUpdate {

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
     * Whether this request's 'done' method has been called.
     */
    private boolean fDone = false;
    
    /**
     * Associated batch operation, or <code>null</code>
     */
    private BatchUpdate fBatchUpdate = null;
    
    protected WorkbenchJob fViewerUpdateJob = new WorkbenchJob("Asynchronous viewer update") { //$NON-NLS-1$
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
    
    /**
     * Constructs an update for the given content provider
     * 
     * @param contentProvider content provider
     * @param elementPath path to associated model element - empty for root element
     * @param element associated model element
     */
    public ViewerUpdateMonitor(ModelContentProvider contentProvider, TreePath elementPath, Object element) {
        fContentProvider = contentProvider;
        fElement = element;
        fElementPath = elementPath;
        // serialize updates per viewer
        fViewerUpdateJob.setRule(getUpdateSchedulingRule());
        fViewerUpdateJob.setSystem(true);
        contentProvider.updateStarted(this);
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
    	if (fBatchUpdate != null) {
    		fBatchUpdate.done(this);
    	} else {
    		scheduleViewerUpdate();
    	}
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
		return fContentProvider.getPresentationContext();
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
	 * Whether this update is rooted at or below the given path.
	 * 
	 * @param path
	 * @return whether this update is rooted at or below the given path
	 */
	abstract boolean isContained(TreePath path);
	
	synchronized void setBatchUpdate(BatchUpdate batchUpdate) {
		fBatchUpdate = batchUpdate;
		batchUpdate.batch(this);
	}
	
}
