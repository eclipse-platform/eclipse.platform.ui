package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * The breakpoint manager manages the collection of breakpoints
 * in the workspace. A breakpoint suspends the execution of a
 * program being debugged. The kinds of breakpoint supported by each
 * debug architecture and the information required to create those
 * breakpoints is dictated by each debug architecture.
 * <p>
 * Breakpoint creation is a client responsibility. Breakpoints
 * are only considered active when registered with the breakpoint manager. 
 * </p>
 * <p>
 * Clients interested in breakpoint change notification may
 * register with the breakpoint manager - see
 * <code>IBreakpointListener</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IBreakpointManager {
	/**
	 * Adds the given breakpoint to the collection of active breakpoints
	 * in the workspace and notifies all registered listeners. This has no effect
	 * if the given breakpoint is already registered.
	 *
	 * @param breakpoint the breakpoint to add
	 *
	 * @exception DebugException if adding fails. Reasons include:<ul>
	 * <li>CONFIGURATION_INVALID - the required <code>MODEL_IDENTIFIER</code> attribute
	 * 	is not set on the breakpoint marker.</li>
	 * <li>A <code>CoreException</code> occurred while verifying the <code>MODEL_IDENTIFIER</code>
	 *	attribute.</li>
	 * </ul>
	 */
	void addBreakpoint(IBreakpoint breakpoint) throws DebugException;
	
	/**
	 * Returns the breakpoint that is associated with marker or
	 * <code>null</code> if no such breakpoint exists
	 * 
	 * @param marker the marker
	 * @return the breakpoint associated with the marker or null if none exists
	 */
	IBreakpoint getBreakpoint(IMarker marker);	
	
	/**
	 * Returns a collection of all existing breakpoints.
	 * Returns an empty array if no breakpoints exist.
	 *
	 * @return an array of breakpoints
	 */
	IBreakpoint[] getBreakpoints();
	
	/**
	 * Returns a collection of all breakpoints registered for the
	 * given debug model. Answers an empty array if no breakpoints are registered
	 * for the given debug model.
	 *
	 * @param modelIdentifier identifier of a debug model plug-in
	 * @return an array of breakpoints
	 */
	IBreakpoint[] getBreakpoints(String modelIdentifier);
		
	/**
	 * Returns whether the given breakpoint is currently
	 * registered with this breakpoint manager.
	 *
	 * @return whether the breakpoint is registered
	 */
	boolean isRegistered(IBreakpoint breakpoint);	

	/**
	 * Removes the given breakpoint from the breakpoint manager, invokes
	 * <code>delete()</code> on the breakpoint if the <code>delete</code> flag
	 * is <code>true</code>, and notifies all registered
	 * listeners. Has no effect if the given breakpoint is not currently
	 * registered.
	 *
	 * @param breakpoint the breakpoint to remove
	 * @param delete whether to delete the given breakpoint
	 * @exception CoreException if an exception occurs while deleting the
	 * 	underlying marker.
	 */
	void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException;

	/**
	 * Adds the given listener to the collection of registered breakpoint listeners.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	void addBreakpointListener(IBreakpointListener listener);

	/**
	 * Removes the given listener from the collection of registered breakpoint listeners.
	 * Has no effect if an identical listener is not already registered.
	 *
	 * @param listener the listener to remove	
	 */
	void removeBreakpointListener(IBreakpointListener listener);
	
}


