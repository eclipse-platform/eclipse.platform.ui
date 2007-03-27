/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * A group of viewer updates to batch together into one UI update job.
 * 
 * @since 3.3
 */
class BatchUpdate {
	
	/**
	 * Pending batched updates
	 */
	private Set fPending = new HashSet();
	
	/**
	 * List of updates needing to run a UI job
	 */
	private List fComplete = null;
	
    protected WorkbenchJob fViewerUpdateJob = new WorkbenchJob("Asynchronous viewer update") { //$NON-NLS-1$
        public IStatus runInUIThread(IProgressMonitor monitor) {
            // necessary to check if viewer is disposed
        	Iterator updates = fComplete.iterator();
    		while (updates.hasNext()) {
    			ViewerUpdateMonitor update = (ViewerUpdateMonitor) updates.next();
		    	try {
		            if (!update.isCanceled() && !update.getContentProvider().isDisposed()) {
		            	IStatus status = update.getStatus();
		                if (status == null || status.isOK()) {
		                	update.performUpdate();
		                }
		            }
		    	} finally {
		    		update.getContentProvider().updateComplete(update);
		    	}
    		}
            return Status.OK_STATUS;
        }
    };	
    
    /**
	 * Creates a new batch update.
	 */
	BatchUpdate() {
		fViewerUpdateJob.setSystem(true);
	}
	
	/**
	 * Batch the given update with this group.
	 * 
	 * @param update update to batch
	 */
	public synchronized void batch(ViewerUpdateMonitor update) {
		fPending.add(update);
	}
	
	/**
	 * Notification the given update is done.
	 * 
	 * @param update update completed
	 */
	public void done(ViewerUpdateMonitor update) {
		boolean allDone = false;
		synchronized (this) {
			fPending.remove(update);
			allDone = fPending.isEmpty();
			if (!update.isCanceled()) {
				if (fComplete == null) {
					fComplete = new ArrayList(fPending.size() + 1);
				}
				fComplete.add(update);
			}
		}
		if (update.isCanceled()) {
			update.getContentProvider().updateComplete(update);
		}
		synchronized (this) {
			if (allDone && fComplete != null) {
				fViewerUpdateJob.schedule();
			}
		}
	}

}
