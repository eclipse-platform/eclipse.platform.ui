package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;

/**
 * The ability to end a debug session with a target program
 * and allow the target to continue running.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IDebugTarget
 */
public interface IDisconnect {
	/**
	 * Returns whether this element can currently disconnect.
	 * 
	 * @return whether this element can currently disconnect
	 */
	public boolean canDisconnect();
	/**
	 * Disconnects this element from its target. Generally, disconnecting
	 * ends a debug session with a debug target, but allows the target
	 * program to continue running.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	public void disconnect() throws DebugException;
	/**
	 * Returns whether this element is disconnected.
	 *
	 * @return whether this element is disconnected
	 */
	public boolean isDisconnected();
}


