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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
	
	// Indicate if the event handler has been shutdown
	private boolean shutdown;

	// Accumulate exceptions that occur
	private ExceptionCollector errors;
	
	/**
	 * Resource event class. The type is specific to subclasses.
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
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("resource: "); //$NON-NLS-1$
			buffer.append(resource.getFullPath());
			buffer.append(" type: "); //$NON-NLS-1$
			buffer.append(getTypeString());
			buffer.append(" depth: "); //$NON-NLS-1$
			buffer.append(getDepthString());
			return buffer.toString();
		}
		protected String getDepthString() {
			switch (depth) {
				case IResource.DEPTH_ZERO :
					return "DEPTH_ZERO"; //$NON-NLS-1$
				case IResource.DEPTH_ONE :
					return "DEPTH_ONE"; //$NON-NLS-1$
				case IResource.DEPTH_INFINITE :
					return "DEPTH_INFINITE"; //$NON-NLS-1$
				default :
					return "INVALID"; //$NON-NLS-1$
			}
		}
		protected String getTypeString() {
			return String.valueOf(type);
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
		createEventHandlingJob();
		schedule();
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
			public boolean shouldRun() {
				return hasUnprocessedEvents();
			}
			public boolean shouldSchedule() {
				return hasUnprocessedEvents();
			}
		};
		eventHandlerJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				jobDone(event);
			}
		});
		eventHandlerJob.setPriority(Job.SHORT);
	}

	/**
	 * This method is invoked when the processing job completes. The
	 * default behavior of the handler is to restart the job if the queue
	 * is no longer empty and to clear the queue if the handler was shut down.
	 */
	protected void jobDone(IJobChangeEvent event) {
		if (isShutdown()) {
			// The handler has been shutdown. Clean up the queue.
			synchronized(this) {
				awaitingProcessing.clear();
			}
		} else if (hasUnprocessedEvents()) {
			// An event squeaked in as the job was finishing. Reschedule the job.
			schedule();
		}
	}
	
	/**
	 * Schedule the job to process the events now.
	 */
	protected void schedule() {
		eventHandlerJob.schedule();
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
	protected abstract String getErrorsTitle();
	
	/**
	 * Shutdown the event handler. Any events on the queue will be removed from the queue
	 * and will not be processed.
	 */
	public void shutdown() {
		shutdown = true;
		eventHandlerJob.cancel();
	}
	
	/**
	 * @return Returns whether the handle has been shutdown.
	 */
	public boolean isShutdown() {
		return shutdown;
	}
	
	/**
	 * Queue the event and start the job if it's not already doing work.
	 */
	protected synchronized void queueEvent(Event event) {
		if (Policy.DEBUG_BACKGROUND_EVENTS) {
			System.out.println("Event queued on " + getName() + ":" + event.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		awaitingProcessing.add(event);
		if (!isShutdown()
			&& eventHandlerJob != null
			&& eventHandlerJob.getState() == Job.NONE) {
			schedule();
		}
	}
	
	/**
	 * Get the next resource to be calculated.
	 * @return Event to be processed
	 */
	private synchronized Event nextElement() {
		if (isShutdown() || !hasUnprocessedEvents()) {
			return null;
		}
		return (Event) awaitingProcessing.remove(0);
	}
	
	/**
	 * Return whether there are unprocessed events on the event queue.
	 * @return whether there are unprocessed events on the queue
	 */
	protected synchronized boolean hasUnprocessedEvents() {
		return !awaitingProcessing.isEmpty();
	}
	
	/**
	 * Process events from the events queue and dispatch results. This method does not
	 * directly check for or handle cancelation of the provided monitor. However,
	 * it does invoke <code>processEvent(Event)</code> which may check for and handle
	 * cancelation by shuting down the receiver.
	 */
	protected IStatus processEvents(IProgressMonitor monitor) {
		errors.clear();
		try {
			// It's hard to know how much work is going to happen
			// since the queue can grow. Use the current queue size as a hint to
			// an infinite progress monitor
			monitor.beginTask(null, 100);
			IProgressMonitor subMonitor = Policy.infiniteSubMonitorFor(monitor, 90);
			subMonitor.beginTask(null, 1024);

			Event event;
			while ((event = nextElement()) != null && ! isShutdown()) {			 	
				try {
					processEvent(event, subMonitor);
					if (Policy.DEBUG_BACKGROUND_EVENTS) {
						System.out.println("Event processed on " + getName() + ":" + event.toString()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (CoreException e) {
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
	 * Handle the exception by recording it in the errors list.
	 * @param e
	 */
	protected void handleException(CoreException e) {
		errors.handleException(e);
		
	}
	
	/**
	 * Process the event in the context of a running background job. Subclasses may
	 * (but are not required to) check the provided monitor for cancelation and shut down the 
	 * receiver by invoking the <code>shutdown()</code> method.
	 * 
	 * @param event
	 * @param monitor
	 */
	protected abstract void processEvent(Event event, IProgressMonitor monitor) throws CoreException;

	/**
	 * @return Returns the eventHandlerJob.
	 */
	public Job getEventHandlerJob() {
		return eventHandlerJob;
	}
}
