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

import org.eclipse.core.internal.plugins.PluginClassLoader;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class Worker extends Thread {
	//worker number used for naming purposes only
	private static int nextWorkerNumber = 0;
	private final WorkerPool pool;
	private volatile Job currentJob;
	
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
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			currentJob = pool.startFirstJob();
			while (currentJob  != null) {
				//if job is null we've been shutdown
				if (currentJob == null)
					return;
				IStatus result = Status.OK_STATUS;
				try {
					result = currentJob.run(((InternalJob)currentJob).getMonitor());
				} catch (OperationCanceledException e) {
					result = Status.CANCEL_STATUS;
				} catch (Exception e) {
					result = handleException(currentJob, e);
				} catch (LinkageError e) {
					result = handleException(currentJob, e);
				} finally {
					//clear interrupted state for this thread
					Thread.interrupted();
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
	private IStatus handleException(Job job, Throwable t) {
		String id;
		try {
			id = ((PluginClassLoader)job.getClass().getClassLoader()).getPluginDescriptor().getUniqueIdentifier();
		} catch (ClassCastException e) {
			//ignore and attribute exception to runtime
			id = Platform.PI_RUNTIME;
		}
		String message = Policy.bind("meta.pluginProblems", id); //$NON-NLS-1$
		return new Status(Status.ERROR, id, Platform.PLUGIN_ERROR, message, t);
	}
}
