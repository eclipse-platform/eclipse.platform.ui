/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * Handles debug events, updating a view and viewer.
 */
public abstract class AbstractDebugEventHandler implements IDebugEventSetListener {
	
	/**
	 * This event handler's view
	 */
	private AbstractDebugView fView;
	
	/**
	 * Queued debug event sets (arrays of events) to process, or <code>null</code> if none.
	 */
	private List fEventSetQueue = new ArrayList();
	
	/**
	 * Update job 
	 */
	private EventProcessingJob fUpdateJob = new EventProcessingJob();
	
	/**
	 * Empty event set constant
	 */
	protected static final DebugEvent[] EMPTY_EVENT_SET = new DebugEvent[0];
	
	/**
	 * Job to dispatch debug event sets
	 */
	private class EventProcessingJob extends UIJob {

	    public EventProcessingJob() {
	        super(DebugUIViewsMessages.getString("AbstractDebugEventHandler.0")); //$NON-NLS-1$
	        setSystem(true);
	    }
	    
        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            boolean more = true;
            int count = 0;
            // to avoid blocking the UI thread, process a max of 50 event sets at once
            while (more && (count < 50)) {
                DebugEvent[] eventSet = null;
			    synchronized (fEventSetQueue) {
			        if (fEventSetQueue.isEmpty()) {
			            return Status.OK_STATUS;
			        }
			        eventSet = (DebugEvent[]) fEventSetQueue.remove(0);
			        more = !fEventSetQueue.isEmpty();
			    }
				if (isAvailable()) {
					if (isViewVisible()) {
						doHandleDebugEvents(eventSet);
					}
					updateForDebugEvents(eventSet);
				}
				count++;
            }
            if (more) {
                // re-schedule with a delay if there are still events to process 
                schedule(50);
            }
            return Status.OK_STATUS;
        }
	    
	}
	/**
	 * Constructs an event handler for the given view.
	 * 
	 * @param view debug view
	 */
	public AbstractDebugEventHandler(AbstractDebugView view) {
		setView(view);
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.addDebugEventListener(this);
	}

	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}
	
	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		if (!isAvailable()) {
			return;
		}
		// filter events
		events = filterEvents(events);
		if (events.length == 0) {
		    return;
		}
		// add the event set to the queue and schedule update
		synchronized (fEventSetQueue) {
		    fEventSetQueue.add(events);
		}
		fUpdateJob.schedule();
	}
	
	/**
	 * Filters the given events before processing.
	 *  
	 * @param events event set received for processing
	 * @return events to be processed
	 */
	protected DebugEvent[] filterEvents(DebugEvent[] events) {
	    return events;
	}
	
	/**
	 * Updates this view for the given debug events. Unlike
	 * doHandleDebugEvents(DebugEvent[]) which is only called if the view is
	 * visible, this method is always called. This allows the view to perform
	 * updating that must always be performed, even when the view is not
	 * visible.
	 */
	protected void updateForDebugEvents(DebugEvent[] events) {
	}
	
	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvents(DebugEvent[] events);	
		
	/**
	 * Helper method for inserting the given element - must be called in UI thread
	 */
	protected void insert(Object element) {
		if (isAvailable()) {
			Object parent= ((ITreeContentProvider)getTreeViewer().getContentProvider()).getParent(element);
			// a parent can be null for a debug target or process that has not yet been associated
			// with a launch
			if (parent != null) {
				getView().showViewer();
				getTreeViewer().add(parent, element);
			}
		}
	}

	/**
	 * Helper method to remove the given element - must be called in UI thread.
	 */
	protected void remove(Object element) {
		if (isAvailable()) {
			getView().showViewer();
			getTreeViewer().remove(element);
		}
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged(Object element) {
		if (isAvailable()) {
			getView().showViewer();
			getTreeViewer().update(element, new String[] {IBasicPropertyConstants.P_TEXT});
		}
	}

	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected void refresh(Object element) {
		if (isAvailable()) {
			 getView().showViewer();
			 getTreeViewer().refresh(element);
		}
	}
	
	/**
	 * Refresh the viewer - must be called in UI thread.
	 */
	public void refresh() {
		if (isAvailable()) {
			 getView().showViewer();
			 getTreeViewer().refresh();
		}
	}	

	/**
	 * Helper method to select and reveal the given element - must be called in UI thread
	 */
	protected void selectAndReveal(Object element) {
		if (isAvailable()) {
			getViewer().setSelection(new StructuredSelection(element), true);
		}
	}
	
	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.removeDebugEventListener(this);
		fEventSetQueue.clear();
	}
	
	/**
	 * Returns the view this event handler is
	 * updating.
	 * 
	 * @return debug view
	 */
	protected AbstractDebugView getView() {
		return fView;
	}
	
	/**
	 * Sets the view this event handler is updating.
	 * 
	 * @param view debug view
	 */
	private void setView(AbstractDebugView view) {
		fView = view;
	}

	/**
	 * Returns the viewer this event handler is 
	 * updating.
	 * 
	 * @return viewer
	 */	
	protected Viewer getViewer() {
		return getView().getViewer();
	}
	
	/**
	 * Returns this event handler's viewer as a tree
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a tree
	 * viewer or <code>null</code> if none
	 */
	protected TreeViewer getTreeViewer() {
		if (getViewer() instanceof TreeViewer) {
			return (TreeViewer)getViewer();
		} 
		return null;
	}
	
	/**
	 * Returns whether this event handler's viewer is
	 * currently available.
	 * 
	 * @return whether this event handler's viewer is
	 * currently available
	 */
	protected boolean isAvailable() {
		return getView().isAvailable();
	}
	
	/**
	 * Returns whether this event handler's view is currently visible.
	 * 
	 * @return whether this event handler's view is currently visible
	 */
	protected boolean isViewVisible() {
		return getView().isVisible();	
	}	
	
	/**
	 * Called when this event handler's view becomes visible. Default behavior
	 * is to refresh the view.
	 */
	protected void viewBecomesVisible() {
		refresh();
	}
	
	/**
	 * Called when this event handler's view becomes hidden. Default behavior is
	 * to do nothing. Subclasses may override.
	 */
	protected void viewBecomesHidden() {
	}

}

