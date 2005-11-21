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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Base implementation of an asynchronous request monitor.
 * <p>
 * Not intended to be subclassed or instantiated by clients. For internal use
 * with the <code>AsynchronousTreeViewer</code> implementation.
 * </p>
 * @since 3.2
 */
abstract class AsynchronousRequestMonitor implements IAsynchronousRequestMonitor {
    
	/**
	 * Widget the upadte is rooted at
	 */
    private Widget fWidget;
    
    /**
     * Viewer the update is being performed for
     */
    private AsynchronousViewer fViewer;
    
    /**
     * Whether this request has been canelled
     */
    private boolean fCanceled = false;
    
    /**
     * Update request status or <code>null</code>
     */
    private IStatus fStatus = null;

    /**
     * Constructs an udpate rooted at the given item.
     * 
     * @param item
     */
    AsynchronousRequestMonitor(Widget item, AsynchronousViewer viewer) {
        fWidget = item;
        fViewer = viewer;
    }
    
    /**
     * Returns the viewer this update is being peformed for
     * 
     * @return the viewer this update is being peformed for
     */
    protected AsynchronousViewer getViewer() {
        return fViewer;
    }
    
    /**
     * Returns the widget this update is rooted at
     * 
     * @return the widget this update is rooted at
     */
    protected Widget getWidget() {
        return fWidget;
    }
    
    /**
     * Returns whether this update contains the given widget.
     * That is, whether this update is for the same widget or a child of
     * the given widget.
     * 
     * @param widget widget to test containment on
     * @return whether this update contains the given widget
     */
    protected boolean contains(Widget widget) {
    	if (widget == getWidget()) {
    		return true;
    	}
        Widget parent = getViewer().getParent(widget);
        while (parent != null) {
            if (parent.equals(getWidget())) {
                return true;
            }
            parent = getViewer().getParent(parent);
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
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
     */
    public void internalWorked(double work) {
        // TODO Auto-generated method stub
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
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
     */
    public void setTaskName(String name) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
     */
    public void subTask(String name) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
     */
    public void worked(int work) {
        // TODO Auto-generated method stub
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IProgressMonitor#done()
     */
    public final void done() {
		if (!isCanceled()) {
			WorkbenchJob job = new WorkbenchJob("AsynchronousRequestMonitor.done()") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					// necessary to check if widget is disposed. The item may
					// have been removed from the tree when another children update
					// occured.
					getViewer().updateComplete(AsynchronousRequestMonitor.this);
					if (!isCanceled() && !getWidget().isDisposed()) {
						if (fStatus != null && !fStatus.isOK()) {
							getViewer().handlePresentationFailure(AsynchronousRequestMonitor.this, fStatus);
						}
						performUpdate();
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
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
    protected abstract boolean contains(AsynchronousRequestMonitor update);
}
