package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;

/**
 * A process represents a program running in normal (non-debug) mode.
 * Processes support setting and getting of client defined attributes.
 * This way, clients can annotate a process with any extra information
 * important to them. For example, classpath annotations, or command
 * line arguments used to launch the process may be important to a client.
 * <p>
 * Clients may implement this interface, however, the debug plug-in
 * provides an implementation of this interface for a
 * <code>java.lang.Process</code>. 
 * </p>
 * @see org.eclipse.debug.core.DebugPlugin#newProcess(ILaunch, Process, String)
 */
public interface IProcess extends IAdaptable, ITerminate {

	/**
	 * Returns a human-readable label for this process.
	 *
	 * @return a label for this process
	 */
	public String getLabel();
	/**
	 * Returns the launch this element originated from.
	 *
	 * @return the launch this process is contained in
	 */
	public ILaunch getLaunch();
	/**
	 * Returns a proxy to the standard input, output, and error streams 
	 * for this process, or <code>null</code> if not supported.
	 *
	 * @return a streams proxy, or <code>null</code> if not supported
	 */
	public IStreamsProxy getStreamsProxy();
	
	/**
	 * Sets the value of a client defined attribute.
	 *
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	public void setAttribute(String key, String value);
	
	/**
	 * Returns the value of a client defined attribute.
	 *
	 * @param key the attribute key
	 * @return value the String attribute value, or <code>null</code> if undefined
	 */
	public String getAttribute(String key);
	
	/**
	 * Returns the exit value of this process. Conventionally, 0 indicates
	 * normal termination.
	 * 
	 * @return the exit value of this process
	 * @exception DebugException if this process has not yet terminated
	 */
	public int getExitValue() throws DebugException;
}
