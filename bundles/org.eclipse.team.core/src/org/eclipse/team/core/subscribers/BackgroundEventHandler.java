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
package org.eclipse.team.core.subscribers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.ExceptionCollector;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * Thsi class provides the infrastucture for processing events in the background 
 */
public abstract class BackgroundEventHandler {
	
	// Events that need to be processed
	private List awaitingProcessing = new ArrayList();
	
	// The job that runs when events need to be processed
	private Job eventHandlerJob;
	
	// Indicate if the event handler has benn shutdown
	private boolean shutdown;

	// manages exceptions
	private ExceptionCollector errors;
	
	/**
	 * Internal resource synchronization event. Can contain a result.
	 */
	public static class Event {
		IResource resource;
		int type;
		int depth;
		public Event(IResource resource, int type, int depth) {
			this.resource = resource;
			this.type = type;
			this.depth = depth;
		}
		public int getDepth() {
			return depth;
		}
		public IResource getResource() {
			return resource;
		}
		public int getType() {
			return type;
		}
	}
	
	
	protected BackgroundEventHandler() {
		errors =
			new ExceptionCollector(
				getErrorsTitle(),
				TeamPlugin.ID,
				IStatus.ERROR,
				null /* don't log */
		);
	}

	/**
	 * Create the event handling job and schedule it
	 */
	protected void initializeEventHandlingJob() {
		createEventHandlingJob();
		schedule();
	}
	
	/**
	 * Handle the exception by recording it in the errors list.
	 * @param e
	 */
	protected void handleException(TeamException e) {
		errors.handleException(e);
		
	}
	/**
	 * Create the job used for processing the events in the queue. The job stops working when
	 * the queue is empty.
	 */
	protected void createEventHandlingJob() {
		eventHandlerJob = new Job(getName()) {	
			public IStatus run(IProgressMonitor monitor) {
				return processEvents(monitor);
			}
		};
		eventHandlerJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				jobDone();
			}
		});
		eventHandlerJob.setPriority(Job.SHORT);
	}

	/**
	 * Return the name of the handler, which is used as the job name.
	 * @return the name of the handler
	 */
	public abstract String getName();
	
	/**
	 * Return the text to be displayed as the title for any errors that occur.
	 * @return
	 */
	public abstract String getErrorsTitle();
	
	/**
	 * Process the event in the context of a background job.
	 * 
	 * @param event
	 * @param monitor
	 */
	protected abstract void processEvent(Event event, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Shutdown the event handler. Any events on the queue will be removed from the queue
	 * and will not be processed.
	 */
	public void shutdown() {
		shutdown = true;
		eventHandlerJob.cancel();
	}
	
	/**
	 * Queue the event and start the job if it's not already doing work.
	 */
	protected synchronized void queueEvent(Event event) {
		awaitingProcessing.add(event);
		if (shutdown
			|| eventHandlerJob == null
			|| eventHandlerJob.getState() != Job.NONE)
			return;
		else {
			schedule();
		}
	}
	
	/**
	 * Get the next resource to be calculated.
	 * @return Event to be processed
	 */
	private synchronized Event nextElement() {
		if (shutdown || awaitingProcessing.isEmpty()) {
			return null;
		}
		return (Event) awaitingProcessing.remove(0);
	}
	
	/**
	 * Process events from the events queue and dispatch results. 
	 */
	protected IStatus processEvents(IProgressMonitor monitor) {
		Event event;
		errors.clear();
		try {
			// It's hard to know how much work is going to happen
			// since the queue can grow. Use the current queue size as a hint to
			// an infinite progress monitor
			monitor.beginTask(null, 100);
			IProgressMonitor subMonitor = Policy.infiniteSubMonitorFor(monitor, 90);
			subMonitor.beginTask(null, 1024);

			while ((event = nextElement()) != null && ! shutdown) {
				// Cancellation is dangerous because this will leave the sync info in a bad state.
				// Purposely not checking -				 	
				try {
					processEvent(event, subMonitor);
				} catch (TeamException e) {
					// handle exception but keep going
					handleException(e);
				}
			}
		} finally {
			monitor.done();
		}
		return errors.getStatus();
	}

	/**
	 * This method is invoked when the processing job completes. The
	 * default behavior of the handler is to restart the job if the queue
	 * is no longer empty and to clear the queue if the handler was shut down.
	 *
	 */
	protected void jobDone() {
		// Make sure an unhandled event didn't squeak in unless we are shutdown
		if (shutdown == false && hasUnprocessedEvents()) {
			schedule();
		} else {
			synchronized(this) {
				awaitingProcessing.clear();
			}
		}
	}
	protected boolean hasUnprocessedEvents() {
		return !awaitingProcessing.isEmpty();
	}
	
	/**
	 * Schedule the job or process the events now.
	 */
	protected void schedule() {
		eventHandlerJob.schedule();
	}

	/**
	 * @return Returns the eventHandlerJob.
	 */
	public Job getEventHandlerJob() {
		return eventHandlerJob;
	}

}
