package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;import java.io.OutputStream;import java.util.HashMap;import org.eclipse.core.runtime.IStatus;import org.eclipse.core.runtime.PlatformObject;import org.eclipse.core.runtime.Status;import org.eclipse.debug.core.DebugEvent;import org.eclipse.debug.core.DebugException;import org.eclipse.debug.core.DebugPlugin;import org.eclipse.debug.core.IDebugConstants;import org.eclipse.debug.core.IDebugStatusConstants;import org.eclipse.debug.core.ILaunch;import org.eclipse.debug.core.model.IProcess;import org.eclipse.debug.core.model.IStreamsProxy;


/**
 * A runtime process is a wrapper for a non-debuggable
 * system process. The process will appear in the debug UI with
 * console and termination support. The process creates a streams
 * proxy for itself, and a process monitor that monitors the
 * underlying system process for terminataion.
 */
public class RuntimeProcess extends PlatformObject implements IProcess {
	
	private final static String PREFIX= "runtime_process.";
	
	private final static String ERROR = PREFIX + "error.";
	private final static String TERMINATE_FAILED = ERROR + "terminate_failed";

	private static final int MAX_WAIT_FOR_DEATH_ATTEMPTS = 10;
	
	/**
	 * The system process
	 */
	protected Process fProcess;
	
	/**
	 * Process monitor
	 */
	protected ProcessMonitor fMonitor;
	
	/**
	 * The streams proxy for this process
	 */
	protected StreamsProxy fStreamsProxy;

	/**
	 * The name of the process
	 */
	protected String fName;

	/**
	 * <code>true</code> when this process has been termianted
	 */
	protected boolean fTerminated;
	
	/**
	 * Table of cleint defined attributes
	 */
	protected HashMap fAttributes;

	/**
	 * Constructs a RuntimeProcess on the given system process
	 * with the given name.
	 */
	public RuntimeProcess(Process process, String name) {
		fProcess= process;
		fName= name;
		fTerminated= true;
		try {
			process.exitValue();
		} catch (IllegalThreadStateException e) {
			fTerminated= false;
		}
		fStreamsProxy = new StreamsProxy(this);
		fMonitor = new ProcessMonitor(this);
		fireCreationEvent();
	}

	/**
	 * @see IProcess
	 */
	public boolean canTerminate() {
		return !fTerminated;
	}

	/**
	 * Returns the error stream of the underlying system process (connected
	 * to the standard error of the process).
	 */
	public InputStream getErrorStream() {
		return fProcess.getErrorStream();
	}

	/**
	 * Returns the input stream of the underlying system process (connected
	 * to the standard out of the process).
	 */
	public InputStream getInputStream() {
		return fProcess.getInputStream();
	}

	/**
	 * @see IProcess
	 */
	public String getLabel() {
		return fName;
	}

	/**
	 * @see IProcess
	 */
	public ILaunch getLaunch() {
		return DebugPlugin.getDefault().getLaunchManager().findLaunch(this);
	}

	/**
	 * Returns the output stream of the underlying system process (connected
	 * to the standard in of the process).
	 */
	public OutputStream getOutputStream() {
		return fProcess.getOutputStream();
	}

	public Process getSystemProcess() {
		return fProcess;
	}

	/**
	 * @see IProcess
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/**
	 * @see IProcess
	 */
	public void terminate() throws DebugException {
		if (!isTerminated()) {
			fProcess.destroy();
			int attempts = 0;
			while (attempts < MAX_WAIT_FOR_DEATH_ATTEMPTS) {
				try {
					fProcess.exitValue();
					return;
				} catch (IllegalThreadStateException ie) {
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
			}
			// clean-up
			fMonitor.stopMonitoring();
			IStatus status = new Status(IStatus.ERROR, IDebugConstants.PLUGIN_ID, IDebugStatusConstants.TARGET_REQUEST_FAILED, TERMINATE_FAILED, null);		
			throw new DebugException(status);
		}
	}

	/**
	 * Notification that the system process associated with this process
	 * has terminated.
	 */
	public void terminated() {
		fStreamsProxy.close();
		fTerminated= true;
		fProcess= null;
		fireTerminateEvent();
	}
		
	public IStreamsProxy getStreamsProxy() {
		return fStreamsProxy;
	}
	
	/**
	 * Fire a debug event marking the creation of this element.
	 */
	public void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fire a debug event
	 */
	public void fireEvent(DebugEvent event) {
		DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEvent(event);
		}
	}

	/**
	 * Fire a debug event marking the termination of this process.
	 */
	public void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * @see IProcess
	 */
	public void setAttribute(String key, String value) {
		if (fAttributes == null) {
			fAttributes = new HashMap(5);
		}
		fAttributes.put(key, value);
	}
	
	/**
	 * @see IProcess
	 */
	public String getAttribute(String key) {
		if (fAttributes == null) {
			return null;
		}
		return (String)fAttributes.get(key);
	}
}

