package org.eclipse.debug.core.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to end a debug session with a target program
 * and allow the target to continue running.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IDebugTarget
 */
public interface IDisconnect {
	/**
	 * Returns whether this element can currently disconnect.
	 * 
	 * @return whether this element can currently disconnect
	 */
	boolean canDisconnect();
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


