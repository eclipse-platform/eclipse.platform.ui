package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * A breakpoint listener is notified of breakpoint additions,
 * removals, and changes. Listeners register and deregister with the
 * breakpoint manager.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see IBreakpointManager
 */

public interface IBreakpointListener {

	/**
	 * Notifies this listener that the given breakpoint has been added
	 * to the breakpoint manager.
	 *
	 * @param breakpoint the added breakpoint
	 * @since 2.0
	 */
	public void breakpointAdded(IBreakpoint breakpoint);
	/**
	 * Notifies this listener that the given breakpoint has been removed
	 * from the breakpoint manager.
	 * If the given breakpoint has been removed because it has been deleted,
	 * the associated marker delta is also provided.
	 *
	 * @param breakpoint the removed breakpoint
	 * @param delta the associated marker delta, or  <code>null</code> when
	 * 	the breakpoint is removed from the breakpoint manager without
	 *	being deleted
	 *
	 * @see org.eclipse.core.resources.IMarkerDelta
	 * @since 2.0
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta);
	
	/**
	 * Notifies this listener that an attribute of the given breakpoint has
	 * changed, as described by the delta.
	 *
	 * @param breakpoint the changed breakpoint
	 * @param delta the marker delta that describes the changes
	 *  with the marker associated with the given breakpoint, or
	 *  <code>null</code> when the breakpoint change does not generate
	 *  a marker delta
	 *
	 * @see org.eclipse.core.resources.IMarkerDelta
	 * @since 2.0
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta);

}