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

 
/**
 * Monitors a system process, wiating for it to terminate, and
 * then notifies the associated runtime process.
 */
public class ProcessMonitor {
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
	/**
	 * Creates a new process monitor and starts monitoring the process
	 * for termination.
	 */
	public ProcessMonitor(RuntimeProcess process) {
		fProcess= process;
		fOSProcess= process.getSystemProcess();
		startMonitoring();
	}

	/**
	 * Monitors the underlying process for termination. When the underlying
	 * process terminates (or if the monitoring thread is interrupted),
	 * inform the <code>IProcess</code> that it has terminated.
	 */
	private void monitorProcess() {
		while (fOSProcess != null) {
			try {
				fOSProcess.waitFor();
			} catch (InterruptedException ie) {
			} finally {
				fOSProcess = null;
				fProcess.terminated();
			}
		}
	}

	/**
	 * Starts monitoring the underlying process to determine
	 * if it has terminated.
	 */
	private void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					monitorProcess();
				}
			}, DebugCoreMessages.getString("ProcessMonitor.label")); //$NON-NLS-1$
			fThread.start();
		}
	}
	
	/**
	 * Kills the monitoring thread.
	 * 
	 * This method is to be useful for dealing with the error
	 * case of an underlying process which has not informed this
	 * monitor of its termination.
	 */
	protected void killMonitoring() {
		fThread.interrupt();
	}
}
