/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

/**
 * 
 */
public class InternalWorker extends Thread {
	private final JobManager manager;
	/**
	 * @GuardedBy("manager.monitorStack")
	 */
	private boolean canceled;

	InternalWorker(JobManager manager) {
		super("Worker-JM"); //$NON-NLS-1$
		this.manager = manager;
	}

	public void run() {
		int timeout = 0;
		synchronized (manager.monitorStack) {
			while (!canceled) {
				if (manager.monitorStack.isEmpty()) {
					timeout = 0;
				} else {
					timeout = 250;
				}
				for (int i = 0; i < manager.monitorStack.size(); i++) {
					Object[] o = (Object[]) manager.monitorStack.get(i);
					IProgressMonitor monitor = (IProgressMonitor) o[1];
					if (monitor.isCanceled()) {
						Job job = (Job) o[0];
						Thread t = job.getThread();
						if (t != null) {
							t.interrupt();
						}
					}
				}
				try {
					manager.monitorStack.wait(timeout);
				} catch (InterruptedException e) {
					// loop
				}
			}
		}
	}

	void cancel() {
		synchronized (manager.monitorStack) {
			canceled = true;
			manager.monitorStack.notifyAll();
		}
	}
}
