package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

 
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;


/**
 * A checked exception representing a failure.
 * <p>
 * Defines status codes relevant to the debug plug-in. When a 
 * debug exception is thrown, it contains a status object describing
 * the cause of the exception. The status objects originating from the
 * debug plug-in use the codes defined in this class.
 * </p>
 * <p>
 * Clients may instantiate this class. Clients are not intended to subclass this class.
 * </p>
 * @see IStatus
 */
public class DebugException extends CoreException {	
	/**
	 * Indicates a request made of a debug element has failed
	 * on the target side.
	 */
	public static final int TARGET_REQUEST_FAILED = 5010;
	 
	/**
	 * Indicates a request is not supported by the capabilities of a debug element.
	 * For example, a request was made to terminate an element that does not
	 * support termination.
	 */
	public static final int NOT_SUPPORTED = 5011;

	/**
	 * Indicates that a request made of manager has failed, or a request made of a
	 * debug element has failed on the client side (that is, before the request was
	 * sent to the debug target).
	 */
	public static final int REQUEST_FAILED = 5012;

	/**
	 * Indicates an internal error. This is an unexpected state.
	 */
	public static final int INTERNAL_ERROR = 5013;
	
	/** 
	 * Indicates an improperly configured breakpoint. Breakpoints have a minimal
	 * set of required attributes as defined by the breakpoint manager.
	 *
	 * @see IBreakpointManager
	 */
	public static final int CONFIGURATION_INVALID = 5014;
	
	/**
	 * Constructs a new debug exception with the given status object.
	 *
	 * @param status the status object describing this exception
	 * @see IStatus
	 */
	public DebugException(IStatus status) {
		super(status);
	}

}
