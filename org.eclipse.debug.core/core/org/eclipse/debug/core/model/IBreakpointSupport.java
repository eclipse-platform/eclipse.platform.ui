package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;

/**
 * Breakpoint support defines functionality for
 * debug targets supporting breakpoints. The breakpoint manager
 * automatically adds debug targets as breakpoint listeners,
 * as launches are registered. The breakpoint
 * manager only informs a debug target of breakpoint changes
 * (that is, add/remove/change) if the target returns <code>true</code>
 * from <code>supportsBreakpoint(IMarker)</code>. The breakpoint manager
 * removes debug targets from its listener list as launches
 * are deregistered.
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
public interface IBreakpointSupport extends IBreakpointListener {
	
	/**
	 * Returns whether this target currently supports the given breakpoint.
	 * The breakpoint manager calls this method before calling an
	 * added/removed/changed method to verify this listener is interested
	 * in the given breakpoint.
	 * <p>
	 * Generally, a debug target is only interested in breakpoints that
	 * originated from its debug model, and only if the debug target has
	 * not been terminated.
	 * </p>
	 *
	 * @param breakpoint the breakpoint being added/removed/changed
	 * @return whether this target is currently interested in the breakpoint
	 */
	boolean supportsBreakpoint(IMarker breakpoint);
	
	/**
	 * Installs the given breakpoint in this target. The breakpoint should
	 * be deferred if it cannot be installed immediately. This method is only
	 * called if this listener supports the given breakpoint.
	 *
	 * @param breakpoint the added breakpoint
	 * @see IBreakpointListener
	 * @see #supportsBreakpoint(IMarker)
	 */
	public void breakpointAdded(IMarker breakpoint);
	
	/**
	 * Uninstalls the given breakpoint from this target if currently installed.
	 * This method is only called if this listener supports the given breakpoint.
	 *
	 * @param breakpoint the removed breakpoint
	 * @see IBreakpointListener 
	 * @see #supportsBreakpoint(IMarker)
	 */
	public void breakpointRemoved(IMarker breakpoint, IMarkerDelta delta);
	
	/**
	 * An attribute of the given breakpoint has changed, as described
	 * by the delta. If the breakpoint is applicable to this target
	 * the attribute change should be reflected. For example, the enabled
	 * state may have changed, and should be updated in the target. This
	 * method is only called if this listener supports the given breakpoint.
	 *
	 * @param breakpoint the changed breakpoint
	 * @see IBreakpointListener
	 * @see #supportsBreakpoint(IMarker)
	 */
	public void breakpointChanged(IMarker breakpoint, IMarkerDelta delta);

}


