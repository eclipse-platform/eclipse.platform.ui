package org.eclipse.debug.internal.core;


/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.debug.core.*;

/**
 * Monitors a system process, wiating for it to terminate, and
 * then notifies the associated runtime process.
 */

public class ProcessMonitor {
	
	private final static String PREFIX= "process_monitor.";
	private final static String LABEL= PREFIX + "label";
	/**
	 * The <code>IProcess</code> being monitored.
	 */
	protected RuntimeProcess fProcess;
	/**
	 * The <code>java.lang.Process</code> being monitored.
	 */
	protected Process fOSProcess;
	/**
	 * The <code>Thread</code> which is monitoring the process.
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
	 * Monitors the process for termination
	 */
	private void monitorProcess() {
		while (fOSProcess != null) {
			try {
				fOSProcess.waitFor();
				fOSProcess= null;
				fProcess.terminated();  
			} catch (InterruptedException ie) {
			}
		}
	}

	/**
	 * Starts monitoring the process to determine
	 * if it has terminated.
	 */
	private void startMonitoring() {
		if (fThread == null) {
			fThread= new Thread(new Runnable() {
				public void run() {
					monitorProcess();
				}
			}, DebugCoreUtils.getResourceString(LABEL));
			fThread.start();
		}
	}

}
