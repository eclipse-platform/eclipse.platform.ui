package org.eclipse.debug.core.model;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to terminate an execution
 * context - for example, a thread, debug target or process.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface ITerminate {
	/**
	 * Returns whether this element can be terminated.
	 *
	 * @return whether this element can be terminated
	 */
	boolean canTerminate();
	/**
	 * Returns whether this element is terminated.
	 *
	 * @return whether this element is terminated
	 */
	public boolean isTerminated();
	/**
	 * Causes this element to terminate.  Implementations may be
	 * blocking or non-blocking.
	 *
	 * @exception DebugException on failure. Reasons include:<ul>
	 * <li>TARGET_REQUEST_FAILED - The request failed in the target
	 * <li>NOT_SUPPORTED - The capability is not supported by the target
	 * </ul>
	 */
	public void terminate() throws DebugException;
}
