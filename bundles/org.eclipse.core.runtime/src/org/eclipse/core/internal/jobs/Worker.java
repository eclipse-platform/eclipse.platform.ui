/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A worker thread processes jobs supplied to it by the worker pool.  When
 * the worker pool gives it a null job, the worker dies.
 */
public class Worker extends Thread {
	//worker number used for debugging purposes only
	private static int nextWorkerNumber = 0;
	private volatile Job currentJob;
	private final WorkerPool pool;

	public Worker(WorkerPool pool) {
		super("Worker-" + nextWorkerNumber++); //$NON-NLS-1$
		this.pool = pool;
	}
	/**
	 * Returns the currently running job, or null if none.
	 */
	public Job currentJob() {
		return currentJob;
	}
	private IStatus handleException(Job job, Throwable t) {
		String message = Policy.bind("jobs.internalError", job.getName()); //$NON-NLS-1$
		return new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, t);
	}
	private void log(IStatus result) {
		try {
			InternalPlatform.log(result);
		} catch (RuntimeException e) {
			//failed to log, so print to console instead
			Throwable t = result.getException();
			if (t != null)
				t.printStackTrace();
		}
	}
	public void run() {
		try {
			currentJob = pool.startFirstJob();
			while (currentJob != null) {
				//if job is null we've been shutdown
				if (currentJob == null)
					return;
				IStatus result = Status.OK_STATUS;
				try {
					result = currentJob.run(((InternalJob) currentJob).getMonitor());
				} catch (OperationCanceledException e) {
					result = Status.CANCEL_STATUS;
				} catch (Exception e) {
					result = handleException(currentJob, e);
				} catch (LinkageError e) {
					result = handleException(currentJob, e);
				} finally {
					//clear interrupted state for this thread
					Thread.interrupted();
					if ((result.getSeverity() & (IStatus.ERROR | IStatus.WARNING)) != 0)
						log(result);
					pool.endJob(currentJob, result);
					currentJob = null;
				}
				currentJob = pool.startJob();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			pool.endWorker(this);
		}
	}
}