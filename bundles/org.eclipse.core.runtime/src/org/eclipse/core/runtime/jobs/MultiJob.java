/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.internal.jobs.Queue;
import org.eclipse.core.runtime.*;

/**
 * A MultiJob processes its own queue of work items until the queue is empty,
 * and schedules itself when necessary.  This ensures that all work is done in
 * a strictly sequential order, and in one thread.
 */
public abstract class MultiJob extends Job {
	private Queue queue = new Queue();
	/**
	 * True when actively processing the work queue or waiting to do so.  False
	 * otherwise (when finished processing the work queue, or when not scheduled).
	 */
	private boolean working;
	private String taskName;
	
	public MultiJob(String name) {
		this.taskName = name == null ? "" : name; //$NON-NLS-1$
		//add a listener to reschedule the job if there is work to do
		addJobChangeListener(new JobChangeAdapter() {
			public void done(Job job, IStatus result) {
				boolean hasWork = false;
				synchronized (queue) {
					hasWork = queue.isEmpty();
				}
				if (hasWork & !working) {
					working = true;
					schedule();
				}
			}
		});
	}
	public final void addWork(Object work) {
		synchronized (queue) {
			queue.enqueue(work);
		}
		if (!working) {
			working = true;
			schedule();
		}
	}
	public abstract IStatus doWork(Object work);
	public final IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
		try {
			Object work = null;
			while (working) {
				synchronized (queue) {
					work = queue.dequeue();
					if (work == null) {
						working = false;
						break;
					}
				}
				doWork(work);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
}