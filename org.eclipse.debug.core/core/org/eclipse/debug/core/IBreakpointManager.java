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
 * program being debugged. The kinds of breakpoints supported by each
 * debug architecture and the information required to create those
 * breakpoints is defined by each debug architecture.
 * Breakpoint creation is a client responsibility.
 * <p>
 * Clients interested in breakpoint change notification may
 * register with the breakpoint manager - see
 * <code>IBreakpointListener</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @see IBreakpointListener
 */
public interface IBreakpointManager {
	/**
	 * Adds the given breakpoint to the collection of registered breakpoints
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
	 * @since 2.0
	 */
	public void addBreakpoint(IBreakpoint breakpoint) throws CoreException;
	
	/**
	 * Returns the breakpoint associated with the given marker or
	 * <code>null</code> if no such breakpoint exists
	 * 
	 * @param marker the marker
	 * @return the breakpoint associated with the marker
	 * 	or <code>null</code> if none exists
	 * @since 2.0
	 */
	public IBreakpoint getBreakpoint(IMarker marker);	
	
	/**
	 * Returns a collection of all registered breakpoints.
	 * Returns an empty array if no breakpoints are registered.
	 *
	 * @return an array of breakpoints
	 * @since 2.0
	 */
	public IBreakpoint[] getBreakpoints();
	
	/**
	 * Returns whether there are any registered breakpoints.
	 * 
	 * @return whether there are any registered breakpoints
	 * @since 2.0
	 */
	public boolean hasBreakpoints();
	
	/**
	 * Returns a collection of all breakpoints registered for the
	 * given debug model. Answers an empty array if no breakpoints are registered
	 * for the given debug model.
	 *
	 * @param modelIdentifier identifier of a debug model plug-in
	 * @return an array of breakpoints
	 * @since 2.0
	 */
	public IBreakpoint[] getBreakpoints(String modelIdentifier);
		
	/**
	 * Returns whether the given breakpoint is currently
	 * registered with this breakpoint manager.
	 *
	 * @return whether the breakpoint is registered
	 * @since 2.0
	 */
	public boolean isRegistered(IBreakpoint breakpoint);
	
	/**
	 * Notifies all registered listeners that the given
	 * breakpoint has changed. Has no effect if the given
	 * breakpoint is not currently registered.
	 * 
	 * This method is intended to be used when a breakpoint
	 * attribute is changed that does not alter the breakpoint's
	 * underlying marker, that is, when notification will not occur
	 * via the marker delta mechanism.
	 * 
	 * @param breakpoint the breakpoint that has changed.
	 * @since 2.0
	 */
	public void fireBreakpointChanged(IBreakpoint breakpoint);

	/**
	 * Removes the given breakpoint from the breakpoint manager, deletes
	 * the marker assocaited with the breakpoint if the <code>delete</code> flag
	 * is <code>true</code>, and notifies all registered
	 * listeners. Has no effect if the given breakpoint is not currently
	 * registered.
	 *
	 * @param breakpoint the breakpoint to remove
	 * @param delete whether to delete the marker associated with the
	 *  breakpoint
	 * @exception CoreException if an exception occurs while deleting the
	 * 	underlying marker.
	 * @since 2.0
	 */
	public void removeBreakpoint(IBreakpoint breakpoint, boolean delete) throws CoreException;

	/**
	 * Adds the given listener to the collection of registered breakpoint listeners.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener the listener to add
	 */
	public void addBreakpointListener(IBreakpointListener listener);

	/**
	 * Removes the given listener from the collection of registered breakpoint listeners.
	 * Has no effect if an identical listener is not already registered.
	 *
	 * @param listener the listener to remove	
	 */
	public void removeBreakpointListener(IBreakpointListener listener);
	
}


