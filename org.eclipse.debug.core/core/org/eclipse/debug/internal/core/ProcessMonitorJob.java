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
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

 
/**
 * Monitors a system process, wiating for it to terminate, and
 * then notifies the associated runtime process.
 */
public class ProcessMonitorJob extends Job {
	/**
	 * The underlying <code>java.lang.Process</code> being monitored.
	 */
	protected Process fOSProcess;	
	/**
	 * The <code>IProcess</code> which will be informed when this
	 * monitor detects that the underlying process has terminated.
	 */
	protected RuntimeProcess fProcess;

	/**
	 * The <code>Thread</code> which is monitoring the underlying process.
	 */
	protected Thread fThread;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		fThread = Thread.currentThread();
		while (fOSProcess != null) {
			try {
				fOSProcess.waitFor();
			} catch (InterruptedException ie) {
			} finally {
				fOSProcess = null;
				fProcess.terminated();
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Creates a new process monitor and starts monitoring the process
	 * for termination.
	 */
	public ProcessMonitorJob(RuntimeProcess process) {
		fProcess= process;
		fOSProcess= process.getSystemProcess();
		schedule();
	}

	/**
	 * Kills the monitoring thread.
	 * 
	 * This method is to be useful for dealing with the error
	 * case of an underlying process which has not informed this
	 * monitor of its termination.
	 */
	protected void killJob() {
		if (fThread == null) {
			cancel();
		} else {
			fThread.interrupt();
		}
	}
}
