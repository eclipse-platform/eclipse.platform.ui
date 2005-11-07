/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTreeViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
	 * Queued debug event sets (arrays of events) to process.
	 */
	private List fEventSetQueue = new ArrayList();
	
	/**
	 * Queued data associated with event sets. Entries may be <code>null</code>.
	 */
	private List fDataQueue = new ArrayList();
	
	/**
	 * Lock to add to/remove from data and event queues.
	 */
	private Object LOCK = new Object();
	
	/**
	 * Update job 
	 */
	private EventProcessingJob fUpdateJob = new EventProcessingJob();
	
	/**
	 * Empty event set constant
	 */
	protected static final DebugEvent[] EMPTY_EVENT_SET = new DebugEvent[0];
	
	private Object NULL = new Object();
	
	/**
	 * Job to dispatch debug event sets
	 */
	private class EventProcessingJob extends UIJob {

        private static final int TIMEOUT = 200;
        
	    public EventProcessingJob() {
	        super(DebugUIViewsMessages.AbstractDebugEventHandler_0);
	        setSystem(true);
	        setPriority(Job.INTERACTIVE);
	    }
	    
        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            boolean more = true;
            long start = System.currentTimeMillis();
            // to avoid blocking the UI thread, process a max of 50 event sets at once
            while (more) {
                DebugEvent[] eventSet = null;
                Object data = null;
			    synchronized (LOCK) {
			        if (fEventSetQueue.isEmpty()) {
			            return Status.OK_STATUS;
			        }
			        eventSet = (DebugEvent[]) fEventSetQueue.remove(0);
			        more = !fEventSetQueue.isEmpty();
			        data = fDataQueue.remove(0);
			        if (data == NULL) {
			            data = null;
			        }
			    }
				if (isAvailable()) {
					if (isViewVisible()) {
						doHandleDebugEvents(eventSet, data);
					}
					updateForDebugEvents(eventSet, data);
				}
                
                if (more) {
                    long current = System.currentTimeMillis();
                    if (current - start > TIMEOUT) {
                        break;
                    }
                }
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
		synchronized (LOCK) {
			events = doPreprocessEvents(events);
			if (events.length == 0) {
			    return;
			}
			// add the event set to the queue and schedule update
		    fEventSetQueue.add(events);
	        if (fDataQueue.size() < fEventSetQueue.size()) {
	            fDataQueue.add(NULL);
	        }
		}
		fUpdateJob.schedule();
	}
	
	protected void queueData(Object data) {
	    synchronized (LOCK) {
	        fDataQueue.add(data);
        }
	}
	
	protected DebugEvent[] doPreprocessEvents(DebugEvent[] events) {
	    return events;
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
	protected void updateForDebugEvents(DebugEvent[] events, Object data) {
	}
	
	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvents(DebugEvent[] events, Object data);	
		
	/**
	 * Helper method for inserting the given element - must be called in UI thread
	 */
	protected void insert(Object element) {
		if (isAvailable()) {
			Viewer viewer = getViewer();
			if (viewer instanceof TreeViewer) {
				TreeViewer tv = (TreeViewer) viewer;
				Object parent = ((ITreeContentProvider)tv.getContentProvider()).getParent(element);
				if (parent != null) {
					getView().showViewer();
					tv.add(parent, element);
				}
			} 
		}
	}

	/**
	 * Helper method to remove the given element - must be called in UI thread.
	 */
	protected void remove(Object element) {
		if (isAvailable()) {
			getView().showViewer();
			Viewer viewer = getViewer();
			if (viewer instanceof TreeViewer) {
				TreeViewer tv = (TreeViewer) viewer;
				tv.remove(element);
			} else if (viewer instanceof AsynchronousTreeViewer) {
				AsynchronousTreeViewer atv = (AsynchronousTreeViewer) viewer;
				atv.refresh();
			}
		}
	}

	/**
	 * Helper method to update the label of the given element - must be called in UI thread
	 */
	protected void labelChanged(Object element) {
		if (isAvailable()) {
			getView().showViewer();
			Viewer viewer = getViewer();
			if (viewer instanceof TreeViewer) {
				TreeViewer tv = (TreeViewer) viewer;
				tv.update(element, new String[] {IBasicPropertyConstants.P_TEXT});
			} else if (viewer instanceof AsynchronousTreeViewer) {
				AsynchronousTreeViewer atv = (AsynchronousTreeViewer) viewer;
				atv.update(element);
			}
		}
	}

	/**
	 * Refresh the given element in the viewer - must be called in UI thread.
	 */
	protected void refresh(Object element) {
		if (isAvailable()) {
			 getView().showViewer();
			 Viewer viewer = getViewer();
			 if (viewer instanceof TreeViewer) {
				 TreeViewer treeViewer = (TreeViewer) viewer;
				 treeViewer.refresh(element);
			 } else if (viewer instanceof AsynchronousTreeViewer) {
				 AsynchronousTreeViewer asyncTreeViewer = (AsynchronousTreeViewer) viewer;
				 asyncTreeViewer.refresh(element);
			 }
		}
	}
	
	/**
	 * Refresh the viewer - must be called in UI thread.
	 */
	public void refresh() {
		if (isAvailable()) {
			 getView().showViewer();
			 getViewer().refresh();
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
		synchronized (LOCK) {
			fEventSetQueue.clear();
			fDataQueue.clear();
		}
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
	 * Returns this event handler's viewer as a structured
	 * viewer or <code>null</code> if none.
	 * 
	 * @return this event handler's viewer as a structured
	 * viewer or <code>null</code> if none
	 */
	protected StructuredViewer getStructuredViewer() {
		if (getViewer() instanceof StructuredViewer) {
			return (StructuredViewer)getViewer();
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

