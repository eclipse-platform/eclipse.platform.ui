package org.eclipse.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
 
/**
 * Defines status codes relevant to the debug plug-in. When a 
 * debug exception is thrown, it contain a status object describing
 * the cause of the exception. The status objects originating from the
 * debug plug-in use the codes defined in this interface.
 * <p>
 * Constants only; not intended to be implemented.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IStatus
 */
public interface IDebugStatusConstants {	
	
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
	 * Indicates that a request made of a debug element or manager has failed
	 * on the client side (that is, before the request was sent to the debug target).
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
	 
}


