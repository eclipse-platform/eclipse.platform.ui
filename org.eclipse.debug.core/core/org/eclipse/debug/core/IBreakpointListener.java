package org.eclipse.debug.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;

/**
 * A breakpoint listener is notified of breakpoint additions,
 * removals, and changes. Listeners register and deregister with the
 * breakpoint manager.
 * <p>
 * Clients may implement this interface.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IBreakpointManager
 */

public interface IBreakpointListener {

	/**
	 * Notifies this listener that the given breakpoint has been added.
	 *
	 * @param breakpoint the added breakpoint
	 */
	public void breakpointAdded(IMarker breakpoint);
	/**
	 * Notifies this listener that the given breakpoint has been removed.
	 * If the given marker has been removed because it has been deleted,
	 * the associated marker delta is also provided such that any attributes 
	 * of the marker can still be accessed.
	 *
	 * @param breakpoint the removed breakpoint
	 * @param delta the associated marker delta, or  <code>null</code> when
	 * 	the breakpoint is removed from the breakpoint manager without
	 *	being deleted
	 *
	 * @see org.eclipse.core.resources.IMarkerDelta
	 */
	public void breakpointRemoved(IMarker breakpoint, IMarkerDelta delta);
	
	/**
	 * Notifies this listener that an attribute of the given breakpoint has
	 * changed, as described by the delta.
	 *
	 * @param breakpoint the changed breakpoint
	 * @param delta the marker delta that describes the change
	 *
	 * @see org.eclipse.core.resources.IMarkerDelta
	 */
	public void breakpointChanged(IMarker breakpoint, IMarkerDelta delta);

}