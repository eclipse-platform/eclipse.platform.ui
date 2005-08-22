/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

/**
 * A worker thread processes jobs supplied to it by the worker pool.  When
 * the worker pool gives it a null job, the worker dies.
 */
public class Worker extends Thread {
	//worker number used for debugging purposes only
	private static int nextWorkerNumber = 0;
	private volatile InternalJob currentJob;
	private final WorkerPool pool;

	public Worker(WorkerPool pool) {
		super("Worker-" + nextWorkerNumber++); //$NON-NLS-1$
		this.pool = pool;
		//set the context loader to avoid leaking the current context loader
		//for the thread that spawns this worker (bug 98376)
		setContextClassLoader(pool.defaultContextLoader);
	}

	/**
	 * Returns the currently running job, or null if none.
	 */
	public Job currentJob() {
		return (Job) currentJob;
	}

	private IStatus handleException(InternalJob job, Throwable t) {
		String message = NLS.bind(Messages.jobs_internalError, job.getName());
		return new Status(IStatus.ERROR, Platform.PI_RUNTIME, Platform.PLUGIN_ERROR, message, t);
	}

	private void log(IStatus result) {
		try {
			//if job is running after shutdown, it has already been logged
			final InternalPlatform platform = InternalPlatform.getDefault();
			if (platform.isRunning()) {
				platform.log(result);
				return;
			}
		} catch (RuntimeException e) {
			//fall through below
		}
		//failed to log, so print to console instead
		Throwable t = result.getException();
		if (t != null)
			t.printStackTrace();
	}

	public void run() {
		setPriority(Thread.NORM_PRIORITY);
		try {
			while ((currentJob = pool.startJob(this)) != null) {
				//if job is null we've been shutdown
				if (currentJob == null)
					return;
				currentJob.setThread(this);
				IStatus result = Status.OK_STATUS;
				try {
					result = currentJob.run(currentJob.getProgressMonitor());
				} catch (OperationCanceledException e) {
					result = Status.CANCEL_STATUS;
				} catch (Exception e) {
					result = handleException(currentJob, e);
				} catch (ThreadDeath e) {
					//must not consume thread death
					throw e;
				} catch (Error e) {
					result = handleException(currentJob, e);
				} finally {
					//clear interrupted state for this thread
					Thread.interrupted();
					//result must not be null
					if (result == null)
						result = handleException(currentJob, new NullPointerException());
					pool.endJob(currentJob, result);
					if ((result.getSeverity() & (IStatus.ERROR | IStatus.WARNING)) != 0)
						log(result);
					currentJob = null;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			currentJob = null;
			pool.endWorker(this);
		}
	}
}
