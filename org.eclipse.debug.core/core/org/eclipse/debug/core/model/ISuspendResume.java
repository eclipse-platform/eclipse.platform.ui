package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to suspend and resume a thread
 * or debug target.
 * <p>
 * Clients may implement this interface.
 * </p>
 */
public interface ISuspendResume {
	/**
	 * Returns whether this element can currently be resumed.
	 *
	 * @return whether this element can currently be resumed
	 */
	public boolean canResume();
	/**
	 * Returns whether this element can currently be suspended.
	 *
	 * @return whether this element can currently be suspended
	 */
	public boolean canSuspend();
	/**
	 * Returns whether this element is currently suspended.
	 *
	 * @return whether this element is currently suspended
	 */
	public boolean isSuspended();
	/**
	 * Causes this element to resume its execution, generating a <code>RESUME</code> event. 
	 * Has no effect on an element that is not suspended. This call is non-blocking.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	public void resume() throws DebugException;
	/**
	 * Causes this element to suspend its execution, generating a <code>SUSPEND</code> event.
	 * Has no effect on an already suspended element.
	 * Implementations may be blocking or non-blocking.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	public void suspend() throws DebugException;
}
