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
package org.eclipse.debug.internal.ui.model.viewers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Base implementation of an asynchronous request monitor.
 * <p>
 * Not intended to be subclassed or instantiated by clients. For internal use
 * with the <code>AsynchronousModelViewer</code> implementation.
 * </p>
 * @since 3.2
 */
public abstract class AsynchronousModelRequestMonitor implements IAsynchronousRequestMonitor {
    
	/**
	 * Model node the upadte is rooted at
	 */
    private ModelNode fNode;
    
    /**
     * Model the update is being performed for
     */
    private AsynchronousModel fModel;
    
    /**
     * Whether this request has been canelled
     */
    private boolean fCanceled = false;
    
    /**
     * Update request status or <code>null</code>
     */
    private IStatus fStatus = null;

    protected WorkbenchJob fViewerUpdateJob = new WorkbenchJob("AsynchronousModelRequestMonitor.fViewerUpdateJob") { //$NON-NLS-1$
        public IStatus runInUIThread(IProgressMonitor monitor) {
            // necessary to check if widget is disposed. The item may
            // have been removed from the tree when another children update
            // occured.
        	getModel().viewerUpdateScheduled(AsynchronousModelRequestMonitor.this);
            getModel().requestComplete(AsynchronousModelRequestMonitor.this);
            if (!isCanceled() && !getNode().isDisposed()) {
                if (fStatus != null && !fStatus.isOK()) {
                	getModel().getViewer().handlePresentationFailure(AsynchronousModelRequestMonitor.this, fStatus);
                }
                performUpdate();
            }
            getModel().viewerUpdateComplete(AsynchronousModelRequestMonitor.this);
            return Status.OK_STATUS;
        }
    };
    
    /**
     * Constructs an udpate rooted at the given item.
     * 
     * @param node model node
     * @param model model the node is in
     */
    public AsynchronousModelRequestMonitor(ModelNode node, AsynchronousModel model) {
        fNode = node;
        fModel = model;
        fViewerUpdateJob.setSystem(true);
    }
    
    /**
     * Returns the model this update is being peformed for
     * 
     * @return the model this update is being peformed for
     */
    protected AsynchronousModel getModel() {
        return fModel;
    }
    
    /**
     * Returns the model node this update is rooted at
     * 
     * @return the model node this update is rooted at
     */
    protected ModelNode getNode() {
        return fNode;
    }
    
    /**
     * Returns whether this update contains the given node.
     * That is, whether this update is for the same node or a child of
     * the given node.
     * 
     * @param ndoe node to test containment on
     * @return whether this update contains the given node
     */
    protected boolean contains(ModelNode node) {
    	if (node == getNode()) {
    		return true;
    	}
        ModelNode parentNode = getNode().getParentNode();
        while (parentNode != null) {
            if (parentNode.equals(getNode())) {
                return true;
            }
            parentNode = parentNode.getParentNode();
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.IAsynchronousRequestMonitor#setStatus(org.eclipse.core.runtime.IStatus)
     */
    public void setStatus(IStatus status) {
        fStatus = status;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
     */
    public void beginTask(String name, int totalWork) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
     */
    public void internalWorked(double work) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
     */
    public boolean isCanceled() {
        return fCanceled;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
     */
    public void setCanceled(boolean value) {
        fCanceled = true;
        getModel().requestCanceled(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
     */
    public void setTaskName(String name) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    public void subTask(String name) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
     */
    public void worked(int work) {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    public final void done() {
		if (!isCanceled()) {
			fViewerUpdateJob.schedule();
		}
	}

    protected void scheduleViewerUpdate(long ms) {
        if(!isCanceled()) 
            fViewerUpdateJob.schedule(ms);
    }
    
    /**
	 * Notification this update has been completed and should now be applied to
	 * this update's viewer. This method is called in the UI thread.
	 */
    protected abstract void performUpdate();
    
    /**
     * Returns whether this update effectively contains the given update.
     * That is, whether this update will also perform the given update.
     * 
     * @param update update to compare to
     * @return whether this update will also perform the given update
     */
    protected abstract boolean contains(AsynchronousModelRequestMonitor update);
    
}
